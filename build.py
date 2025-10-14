import os
import shutil
import zipfile
import platform

BUILD_VERSION = "3.2.0"

SERVER_VERSION = "1.1.2"

# 1. Check if the "Build" folder exists.
if os.path.exists("Build"):
    # 2. If it does, delete everything inside.
    for item in os.listdir("Build"):
        item_path = os.path.join("Build", item)
        if os.path.isfile(item_path) or os.path.islink(item_path):
            os.unlink(item_path)
        elif os.path.isdir(item_path):
            shutil.rmtree(item_path)
else:
    # Otherwise, create the "Build" folder.
    os.makedirs("Build")

if os.path.exists("infinicraft-mod/build/libs"):
    for item in os.listdir("infinicraft-mod/build/libs"):
        item_path = os.path.join("infinicraft-mod/build/libs", item)
        if os.path.isfile(item_path) or os.path.islink(item_path):
            os.unlink(item_path)
        elif os.path.isdir(item_path):
            shutil.rmtree(item_path)

# 3. Change directory into the "infinicraft-mod" folder.
os.chdir("infinicraft-mod")

# 4. Change the "mod_version" variable in "gradle.properties" to BUILD_VERSION.
gradle_properties_path = "gradle.properties"
with open(gradle_properties_path, "r") as file:
    lines = file.readlines()

with open(gradle_properties_path, "w") as file:
    for line in lines:
        if line.startswith("mod_version"):
            file.write(f"mod_version={BUILD_VERSION}\n")
        else:
            file.write(line)

# 5. Run Gradle build.
if platform.system() == "Windows":
    gradle_wrapper = "gradlew.bat"
else:
    gradle_wrapper = "./gradlew"
    os.chmod("./gradlew", 0o755)
os.system(f"{gradle_wrapper} build")

# 6. Change directory back out of the "infinicraft-mod" folder.
os.chdir("..")

# 7. Copy the contents of "infinicraft-mod/build/libs" into the "Build" folder.
libs_path = "infinicraft-mod/build/libs"
if os.path.exists(libs_path):
    for item in os.listdir(libs_path):
        shutil.copy(os.path.join(libs_path, item), "Build")

# 8. Delete "infinicraft-BUILD_VERSION-sources.jar" in the "Build" folder.
sources_jar = f"Build/infinicraft-{BUILD_VERSION}-sources.jar"
if os.path.exists(sources_jar):
    os.remove(sources_jar)

# 9. Zip the contents of the "infinicraft-server" folder into "infinicraftServer_vSERVER_VERSION.py".
server_folder = "infinicraft-server"
zip_file_path = f"Build/infinicraftServer_v{SERVER_VERSION}.zip"

with zipfile.ZipFile(zip_file_path, "w", zipfile.ZIP_DEFLATED) as zipf:
    for root, dirs, files in os.walk(server_folder):
        for file in files:
            file_path = os.path.join(root, file)
            arcname = os.path.relpath(file_path, server_folder)
            zipf.write(file_path, arcname)

print("Build process completed successfully.")
