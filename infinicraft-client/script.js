// Imports
const axios = require('axios');
const fs = require('fs');
const OpenAI = require('openAI')

const openai = new OpenAI({
  apiKey: 'YOUR_API_KEY'
})

// Settings
const url = undefined; // The texture server's URL (leave blank for no server)

// Script
console.log('Listening for file changes...')

// Wait for new recipes to be added
// Craft queue stores all unresolved recipes, sent from the mod
const craftCache = []
fs.watch('craftQueue.json', {}, (event) => {
  if (event != 'change') return

  const file = fs.readFileSync('craftQueue.json', {encoding: 'utf-8'})
  if (!file.length) return // empty file

  const queue = JSON.parse(file)
  if (!queue.length) return // empty queue

  // try to craft everything in the queue
  queue.forEach(recipe => {
    if (craftCache.indexOf(recipe.join(' + ')) != -1) return; // ignore duplicates in queue

    craftCache.push(recipe.join(' + ')) // add this recipe to the cache

    craft(recipe) // request a craft
  })

  fs.writeFileSync('craftQueue.json', '[]') // empty the queue
})

// Waits for new items to be added
// Items are added when by craft(), when a new recipe comes in with a new item
const textureCache = []
fs.watch('items.json', {}, (event) => {
  if (event != 'change') return;

  const file = fs.readFileSync('items.json', {encoding: 'utf-8'})
  if (!file.length) return

  let items; // the list of items to be added

  try {
    items = JSON.parse(file)
  } catch (e) {
    console.log(e)
  }

  if (!items) return;

  // if its a custom item and it doesn't have a texture, request one
  // get all custom items w/o textures and not in cache
  if (!url) return // no server

  const missingTextures = items.filter((item) => item.custom && !item.texture && textureCache.indexOf(item.item) == -1)

  for (let item of missingTextures) {
    textureCache.push(item) // add to cache
    texture(item) // request a texture the item
  }
})

// Requests the server to craft (GPT) a new item
// Takes in an array of item strings
async function craft(items) {
  const recipe = items.join(' + ')
  console.log('Crafting: ' + recipe)

  const prompt = fs.readFileSync('./prompt.txt', 'utf-8')
  const messages = [
    {"role": "system", "content": prompt},
    {"role": "user", "content": recipe}
  ]

  // query chatGPT
  const completion = await openai.chat.completions.create({
    model: 'gpt-3.5-turbo',
    messages,
    temperature: 0.75
  })
  
  const output = JSON.parse(completion.choices[0].message.content)

  const itemName = output.item // the new item
  const itemColor = output.color // the new item's color
  
  console.log(`Item crafted: ${recipe} = ${itemName}`);
  
  // add the recipe to the recipe JSON
  const recipes = JSON.parse(fs.readFileSync('recipes.json', 'utf-8'))
  
  recipes.push({
    input: items, // the input items
    output: itemName, // the output items
    color: itemColor // the item's color
  })

  fs.writeFileSync('recipes.json', JSON.stringify(recipes, null, 4))

  // add the item to items JSON
  const itemsList = JSON.parse(fs.readFileSync('items.json', 'utf-8'))

  if (itemsList.filter((itemObject) => itemObject.item == itemName).length) return // item is already registered
  itemsList.push(output)

  fs.writeFileSync('items.json', JSON.stringify(itemsList, null, 4))
}

// Requests the server to texture an item
// Server should respond with an array that represents the item's texture
function texture(itemObject) {
  console.log('Requesting texture for: ' + itemObject.item)

  axios.post(url + 'texture', itemObject)
  .then(response => {
    console.log('Received texture for ' + itemObject.item)

    // add texture to the item in the items doc
    const items = JSON.parse(fs.readFileSync('items.json', {encoding: 'utf-8'}))

    items.filter((i) => i.item == itemObject.item)[0].texture = response.data

    fs.writeFileSync('items.json', JSON.stringify(items, null, 4))
  })
  .catch(error => {
    console.error('Error:', error);

    // if at first you don't succeed, try again!
    texture(itemObject)
  });
}