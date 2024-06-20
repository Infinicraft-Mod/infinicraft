package net.spiralio.models;

import net.spiralio.util.CaseInsensitiveString;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MissingTextures {

    private static final int[] colorless = new int[] { -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,9544637,9544637,9544637,9544637,9544637,9544637,-1,-1,-1,-1,-1,-1,-1,-1,9544637,9544637,16777215,16777215,16777215,16777215,16777215,16777215,9544637,9544637,-1,-1,-1,-1,-1,9544637,16777215,16777215,14805231,16277079,16277079,16277079,16277079,14805231,16777215,16777215,9544637,-1,-1,-1,-1,7240607,16777215,14805231,16277079,16277079,16277079,16277079,16277079,16277079,14805231,16777215,7240607,-1,-1,-1,7240607,16777215,14805231,16053751,16000332,16000332,16053751,16053751,16000332,16000332,16053751,14805231,16777215,7240607,-1,-1,7240607,16777215,14805231,16053751,16053751,16053751,16053751,16053751,16000332,16000332,16053751,14805231,16777215,7240607,-1,-1,4936314,14805231,14805231,16053751,16053751,16053751,16053751,16000332,16000332,16000332,16053751,14805231,14805231,4936314,-1,-1,4936314,11651036,14805231,14805231,16053751,16053751,16000332,16000332,16000332,16053751,14805231,14805231,11651036,4936314,-1,-1,4936314,11651036,14805231,14805231,14805231,16053751,16000332,16000332,16053751,14805231,14805231,14805231,11651036,4936314,-1,-1,4936314,11651036,11651036,14805231,14805231,14805231,14805231,14805231,14805231,14805231,14805231,11651036,11651036,4936314,-1,-1,-1,4936314,11651036,11651036,11651036,14805231,16000332,16000332,14805231,11651036,11651036,11651036,4936314,-1,-1,-1,-1,4936314,8492989,11651036,11651036,11651036,11473750,11473750,11651036,11651036,11651036,8492989,4936314,-1,-1,-1,-1,-1,4936314,4936314,8492989,11651036,11651036,11651036,11651036,8492989,4936314,4936314,-1,-1,-1,-1,-1,-1,-1,-1,4936314,4936314,4936314,4936314,4936314,4936314,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1 };

    private final static HashMap<CaseInsensitiveString, int[]> colors = new HashMap<>() {{
        put(new CaseInsensitiveString("black"), new int[] { -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0,0,0,0,-1,-1,-1,-1,-1,-1,-1,-1,0,0,4013373,4013373,4013373,4013373,4013373,4013373,0,0,-1,-1,-1,-1,-1,0,4013373,4013373,2041389,16053751,16053751,16053751,16053751,2041389,4013373,4013373,0,-1,-1,-1,-1,0,4013373,2041389,16053751,16053751,16053751,16053751,16053751,16053751,2041389,4013373,0,-1,-1,-1,0,4013373,2041389,3289909,14805231,14805231,3289909,3289909,14805231,14805231,3289909,2041389,4013373,0,-1,-1,0,4013373,2041389,3289909,3289909,3289909,3289909,3289909,14805231,14805231,3289909,2041389,4013373,0,-1,-1,0,2041389,2041389,3289909,3289909,3289909,3289909,14805231,14805231,14805231,3289909,2041389,2041389,0,-1,-1,0,1306,2041389,2041389,3289909,3289909,14805231,14805231,14805231,3289909,2041389,2041389,1306,0,-1,-1,0,1306,2041389,2041389,2041389,3289909,14805231,14805231,3289909,2041389,2041389,2041389,1306,0,-1,-1,0,1306,1306,2041389,2041389,2041389,2041389,2041389,2041389,2041389,2041389,1306,1306,0,-1,-1,-1,0,1306,1306,1306,2041389,14805231,14805231,2041389,1306,1306,1306,0,-1,-1,-1,-1,0,0,1306,1306,1306,11651036,11651036,1306,1306,1306,0,0,-1,-1,-1,-1,-1,0,0,0,1306,1306,1306,1306,0,0,0,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0,0,0,0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1 });
        put(new CaseInsensitiveString("blue"), new int[] { -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,26003,26003,26003,26003,26003,26003,-1,-1,-1,-1,-1,-1,-1,-1,26003,26003,49621,49621,49621,49621,49621,49621,26003,26003,-1,-1,-1,-1,-1,26003,49621,49621,43717,16053751,16053751,16053751,16053751,43717,49621,49621,26003,-1,-1,-1,-1,15733,49621,43717,16053751,16053751,16053751,16053751,16053751,16053751,43717,49621,15733,-1,-1,-1,15733,49621,43717,47053,14805231,14805231,47053,47053,14805231,14805231,47053,43717,49621,15733,-1,-1,15733,49621,43717,47053,47053,47053,47053,47053,14805231,14805231,47053,43717,49621,15733,-1,-1,5200,43717,43717,47053,47053,47053,47053,14805231,14805231,14805231,47053,43717,43717,5200,-1,-1,5200,35250,43717,43717,47053,47053,14805231,14805231,14805231,47053,43717,43717,35250,5200,-1,-1,5200,35250,43717,43717,43717,47053,14805231,14805231,47053,43717,43717,43717,35250,5200,-1,-1,5200,35250,35250,43717,43717,43717,43717,43717,43717,43717,43717,35250,35250,5200,-1,-1,-1,5200,35250,35250,35250,43717,14805231,14805231,43717,35250,35250,35250,5200,-1,-1,-1,-1,5200,22931,35250,35250,35250,11651036,11651036,35250,35250,35250,22931,5200,-1,-1,-1,-1,-1,5200,5200,22931,35250,35250,35250,35250,22931,5200,5200,-1,-1,-1,-1,-1,-1,-1,-1,5200,5200,5200,5200,5200,5200,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1 });
        put(new CaseInsensitiveString("green"), new int[] { -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,30976,30976,30976,30976,30976,30976,-1,-1,-1,-1,-1,-1,-1,-1,30976,30976,5821696,5821696,5821696,5821696,5821696,5821696,30976,30976,-1,-1,-1,-1,-1,30976,5821696,5821696,3849728,16053751,16053751,16053751,16053751,3849728,5821696,5821696,30976,-1,-1,-1,-1,20736,5821696,3849728,16053751,16053751,16053751,16053751,16053751,16053751,3849728,5821696,20736,-1,-1,-1,20736,5821696,3849728,5098240,14805231,14805231,5098240,5098240,14805231,14805231,5098240,3849728,5821696,20736,-1,-1,20736,5821696,3849728,5098240,5098240,5098240,5098240,5098240,14805231,14805231,5098240,3849728,5821696,20736,-1,-1,10240,3849728,3849728,5098240,5098240,5098240,5098240,14805231,14805231,14805231,5098240,3849728,3849728,10240,-1,-1,10240,695552,3849728,3849728,5098240,5098240,14805231,14805231,14805231,5098240,3849728,3849728,695552,10240,-1,-1,10240,695552,3849728,3849728,3849728,5098240,14805231,14805231,5098240,3849728,3849728,3849728,695552,10240,-1,-1,10240,695552,695552,3849728,3849728,3849728,3849728,3849728,3849728,3849728,3849728,695552,695552,10240,-1,-1,-1,10240,695552,695552,695552,3849728,14805231,14805231,3849728,695552,695552,695552,10240,-1,-1,-1,-1,10240,27904,695552,695552,695552,11651036,11651036,695552,695552,695552,27904,10240,-1,-1,-1,-1,-1,10240,10240,27904,695552,695552,695552,695552,27904,10240,10240,-1,-1,-1,-1,-1,-1,-1,-1,10240,10240,10240,10240,10240,10240,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1 });
        put(new CaseInsensitiveString("orange"), new int[] { -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,8988160,8988160,8988160,8988160,8988160,8988160,-1,-1,-1,-1,-1,-1,-1,-1,8988160,8988160,16220697,16220697,16220697,16220697,16220697,16220697,8988160,8988160,-1,-1,-1,-1,-1,8988160,16220697,16220697,14248713,16053751,16053751,16053751,16053751,14248713,16220697,16220697,8988160,-1,-1,-1,-1,6684672,16220697,14248713,16053751,16053751,16053751,16053751,16053751,16053751,14248713,16220697,6684672,-1,-1,-1,6684672,16220697,14248713,15497233,14805231,14805231,15497233,15497233,14805231,14805231,15497233,14248713,16220697,6684672,-1,-1,6684672,16220697,14248713,15497233,15497233,15497233,15497233,15497233,14805231,14805231,15497233,14248713,16220697,6684672,-1,-1,4390912,14248713,14248713,15497233,15497233,15497233,15497233,14805231,14805231,14805231,15497233,14248713,14248713,4390912,-1,-1,4390912,11094528,14248713,14248713,15497233,15497233,14805231,14805231,14805231,15497233,14248713,14248713,11094528,4390912,-1,-1,4390912,11094528,14248713,14248713,14248713,15497233,14805231,14805231,15497233,14248713,14248713,14248713,11094528,4390912,-1,-1,4390912,11094528,11094528,14248713,14248713,14248713,14248713,14248713,14248713,14248713,14248713,11094528,11094528,4390912,-1,-1,-1,4390912,11094528,11094528,11094528,14248713,14805231,14805231,14248713,11094528,11094528,11094528,4390912,-1,-1,-1,-1,4390912,7936512,11094528,11094528,11094528,11651036,11651036,11094528,11094528,11094528,7936512,4390912,-1,-1,-1,-1,-1,4390912,4390912,7936512,11094528,11094528,11094528,11094528,7936512,4390912,4390912,-1,-1,-1,-1,-1,-1,-1,-1,4390912,4390912,4390912,4390912,4390912,4390912,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1 });
        put(new CaseInsensitiveString("purple"), new int[] { -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,4456595,4456595,4456595,4456595,4456595,4456595,-1,-1,-1,-1,-1,-1,-1,-1,4456595,4456595,11665621,11665621,11665621,11665621,11665621,11665621,4456595,4456595,-1,-1,-1,-1,-1,4456595,11665621,11665621,9699525,16053751,16053751,16053751,16053751,9699525,11665621,11665621,4456595,-1,-1,-1,-1,2162805,11665621,9699525,16053751,16053751,16053751,16053751,16053751,16053751,9699525,11665621,2162805,-1,-1,-1,2162805,11665621,9699525,10944717,14805231,14805231,10944717,10944717,14805231,14805231,10944717,9699525,11665621,2162805,-1,-1,2162805,11665621,9699525,10944717,10944717,10944717,10944717,10944717,14805231,14805231,10944717,9699525,11665621,2162805,-1,-1,80,9699525,9699525,10944717,10944717,10944717,10944717,14805231,14805231,14805231,10944717,9699525,9699525,80,-1,-1,80,6553778,9699525,9699525,10944717,10944717,14805231,14805231,14805231,10944717,9699525,9699525,6553778,80,-1,-1,80,6553778,9699525,9699525,9699525,10944717,14805231,14805231,10944717,9699525,9699525,9699525,6553778,80,-1,-1,80,6553778,6553778,9699525,9699525,9699525,9699525,9699525,9699525,9699525,9699525,6553778,6553778,80,-1,-1,-1,80,6553778,6553778,6553778,9699525,14805231,14805231,9699525,6553778,6553778,6553778,80,-1,-1,-1,-1,80,3408019,6553778,6553778,6553778,11651036,11651036,6553778,6553778,6553778,3408019,80,-1,-1,-1,-1,-1,80,80,3408019,6553778,6553778,6553778,6553778,3408019,80,80,-1,-1,-1,-1,-1,-1,-1,-1,80,80,80,80,80,80,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1 });
        put(new CaseInsensitiveString("red"), new int[] { -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,7471104,7471104,7471104,7471104,7471104,7471104,-1,-1,-1,-1,-1,-1,-1,-1,7471104,7471104,14695192,14695192,14695192,14695192,14695192,14695192,7471104,7471104,-1,-1,-1,-1,-1,7471104,14695192,14695192,12723208,16053751,16053751,16053751,16053751,12723208,14695192,14695192,7471104,-1,-1,-1,-1,5177344,14695192,12723208,16053751,16053751,16053751,16053751,16053751,16053751,12723208,14695192,5177344,-1,-1,-1,5177344,14695192,12723208,13971728,14805231,14805231,13971728,13971728,14805231,14805231,13971728,12723208,14695192,5177344,-1,-1,5177344,14695192,12723208,13971728,13971728,13971728,13971728,13971728,14805231,14805231,13971728,12723208,14695192,5177344,-1,-1,2883584,12723208,12723208,13971728,13971728,13971728,13971728,14805231,14805231,14805231,13971728,12723208,12723208,2883584,-1,-1,2883584,9569024,12723208,12723208,13971728,13971728,14805231,14805231,14805231,13971728,12723208,12723208,9569024,2883584,-1,-1,2883584,9569024,12723208,12723208,12723208,13971728,14805231,14805231,13971728,12723208,12723208,12723208,9569024,2883584,-1,-1,2883584,9569024,9569024,12723208,12723208,12723208,12723208,12723208,12723208,12723208,12723208,9569024,9569024,2883584,-1,-1,-1,2883584,9569024,9569024,9569024,12723208,14805231,14805231,12723208,9569024,9569024,9569024,2883584,-1,-1,-1,-1,2883584,6422528,9569024,9569024,9569024,11651036,11651036,9569024,9569024,9569024,6422528,2883584,-1,-1,-1,-1,-1,2883584,2883584,6422528,9569024,9569024,9569024,9569024,6422528,2883584,2883584,-1,-1,-1,-1,-1,-1,-1,-1,2883584,2883584,2883584,2883584,2883584,2883584,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1 });
        put(new CaseInsensitiveString("yellow"), new int[] { -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,16152576,16152576,16152576,16152576,16152576,16152576,-1,-1,-1,-1,-1,-1,-1,-1,16152576,16152576,16700160,16700160,16700160,16700160,16700160,16700160,16152576,16152576,-1,-1,-1,-1,-1,16152576,16700160,16700160,16563200,16053751,16053751,16053751,16053751,16563200,16700160,16700160,16152576,-1,-1,-1,-1,11685888,16700160,16563200,16053751,16053751,16053751,16053751,16053751,16053751,16563200,16700160,11685888,-1,-1,-1,11685888,16700160,16563200,16632064,14805231,14805231,16632064,16632064,14805231,14805231,16632064,16563200,16700160,11685888,-1,-1,11685888,16700160,16563200,16632064,16632064,16632064,16632064,16632064,14805231,14805231,16632064,16563200,16700160,11685888,-1,-1,7218688,16563200,16563200,16632064,16632064,16632064,16632064,14805231,14805231,14805231,16632064,16563200,16563200,7218688,-1,-1,7218688,16358400,16563200,16563200,16632064,16632064,14805231,14805231,14805231,16632064,16563200,16563200,16358400,7218688,-1,-1,7218688,16358400,16563200,16563200,16563200,16632064,14805231,14805231,16632064,16563200,16563200,16563200,16358400,7218688,-1,-1,7218688,16358400,16358400,16563200,16563200,16563200,16563200,16563200,16563200,16563200,16563200,16358400,16358400,7218688,-1,-1,-1,7218688,16358400,16358400,16358400,16563200,14805231,14805231,16563200,16358400,16358400,16358400,7218688,-1,-1,-1,-1,7218688,14117888,16358400,16358400,16358400,11651036,11651036,16358400,16358400,16358400,14117888,7218688,-1,-1,-1,-1,-1,7218688,7218688,14117888,16358400,16358400,16358400,16358400,14117888,7218688,7218688,-1,-1,-1,-1,-1,-1,-1,-1,7218688,7218688,7218688,7218688,7218688,7218688,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1 });
    }};

    // avoid creating lots of 256-length arrays for every possible color unless we need it. this will use substantially less idle memory
    /*
    CSS color codes from https://github.com/bahamas10/css-color-names:

    Copyright 2018 Dave Eddy <dave@daveeddy.com>

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
     */
    private final static HashMap<CaseInsensitiveString, Color> colorTemplates = new HashMap<>() {{
        put(new CaseInsensitiveString("aliceblue"), new Color(0xf0f8ff));
        put(new CaseInsensitiveString("antiquewhite"), new Color(0xfaebd7));
        put(new CaseInsensitiveString("aqua"), new Color(0x00ffff));
        put(new CaseInsensitiveString("aquamarine"), new Color(0x7fffd4));
        put(new CaseInsensitiveString("azure"), new Color(0xf0ffff));
        put(new CaseInsensitiveString("beige"), new Color(0xf5f5dc));
        put(new CaseInsensitiveString("bisque"), new Color(0xffe4c4));
        put(new CaseInsensitiveString("black"), new Color(0x000000));
        put(new CaseInsensitiveString("blanchedalmond"), new Color(0xffebcd));
        put(new CaseInsensitiveString("blue"), new Color(0x0000ff));
        put(new CaseInsensitiveString("blueviolet"), new Color(0x8a2be2));
        put(new CaseInsensitiveString("brown"), new Color(0xa52a2a));
        put(new CaseInsensitiveString("burlywood"), new Color(0xdeb887));
        put(new CaseInsensitiveString("cadetblue"), new Color(0x5f9ea0));
        put(new CaseInsensitiveString("chartreuse"), new Color(0x7fff00));
        put(new CaseInsensitiveString("chocolate"), new Color(0xd2691e));
        put(new CaseInsensitiveString("coral"), new Color(0xff7f50));
        put(new CaseInsensitiveString("cornflowerblue"), new Color(0x6495ed));
        put(new CaseInsensitiveString("cornsilk"), new Color(0xfff8dc));
        put(new CaseInsensitiveString("crimson"), new Color(0xdc143c));
        put(new CaseInsensitiveString("cyan"), new Color(0x00ffff));
        put(new CaseInsensitiveString("darkblue"), new Color(0x00008b));
        put(new CaseInsensitiveString("darkcyan"), new Color(0x008b8b));
        put(new CaseInsensitiveString("darkgoldenrod"), new Color(0xb8860b));
        put(new CaseInsensitiveString("darkgray"), new Color(0xa9a9a9));
        put(new CaseInsensitiveString("darkgreen"), new Color(0x006400));
        put(new CaseInsensitiveString("darkgrey"), new Color(0xa9a9a9));
        put(new CaseInsensitiveString("darkkhaki"), new Color(0xbdb76b));
        put(new CaseInsensitiveString("darkmagenta"), new Color(0x8b008b));
        put(new CaseInsensitiveString("darkolivegreen"), new Color(0x556b2f));
        put(new CaseInsensitiveString("darkorange"), new Color(0xff8c00));
        put(new CaseInsensitiveString("darkorchid"), new Color(0x9932cc));
        put(new CaseInsensitiveString("darkred"), new Color(0x8b0000));
        put(new CaseInsensitiveString("darksalmon"), new Color(0xe9967a));
        put(new CaseInsensitiveString("darkseagreen"), new Color(0x8fbc8f));
        put(new CaseInsensitiveString("darkslateblue"), new Color(0x483d8b));
        put(new CaseInsensitiveString("darkslategray"), new Color(0x2f4f4f));
        put(new CaseInsensitiveString("darkslategrey"), new Color(0x2f4f4f));
        put(new CaseInsensitiveString("darkturquoise"), new Color(0x00ced1));
        put(new CaseInsensitiveString("darkviolet"), new Color(0x9400d3));
        put(new CaseInsensitiveString("deeppink"), new Color(0xff1493));
        put(new CaseInsensitiveString("deepskyblue"), new Color(0x00bfff));
        put(new CaseInsensitiveString("dimgray"), new Color(0x696969));
        put(new CaseInsensitiveString("dimgrey"), new Color(0x696969));
        put(new CaseInsensitiveString("dodgerblue"), new Color(0x1e90ff));
        put(new CaseInsensitiveString("firebrick"), new Color(0xb22222));
        put(new CaseInsensitiveString("floralwhite"), new Color(0xfffaf0));
        put(new CaseInsensitiveString("forestgreen"), new Color(0x228b22));
        put(new CaseInsensitiveString("fuchsia"), new Color(0xff00ff));
        put(new CaseInsensitiveString("gainsboro"), new Color(0xdcdcdc));
        put(new CaseInsensitiveString("ghostwhite"), new Color(0xf8f8ff));
        put(new CaseInsensitiveString("goldenrod"), new Color(0xdaa520));
        put(new CaseInsensitiveString("gold"), new Color(0xffd700));
        put(new CaseInsensitiveString("gray"), new Color(0x808080));
        put(new CaseInsensitiveString("green"), new Color(0x008000));
        put(new CaseInsensitiveString("greenyellow"), new Color(0xadff2f));
        put(new CaseInsensitiveString("grey"), new Color(0x808080));
        put(new CaseInsensitiveString("honeydew"), new Color(0xf0fff0));
        put(new CaseInsensitiveString("hotpink"), new Color(0xff69b4));
        put(new CaseInsensitiveString("indianred"), new Color(0xcd5c5c));
        put(new CaseInsensitiveString("indigo"), new Color(0x4b0082));
        put(new CaseInsensitiveString("ivory"), new Color(0xfffff0));
        put(new CaseInsensitiveString("khaki"), new Color(0xf0e68c));
        put(new CaseInsensitiveString("lavenderblush"), new Color(0xfff0f5));
        put(new CaseInsensitiveString("lavender"), new Color(0xe6e6fa));
        put(new CaseInsensitiveString("lawngreen"), new Color(0x7cfc00));
        put(new CaseInsensitiveString("lemonchiffon"), new Color(0xfffacd));
        put(new CaseInsensitiveString("lightblue"), new Color(0xadd8e6));
        put(new CaseInsensitiveString("lightcoral"), new Color(0xf08080));
        put(new CaseInsensitiveString("lightcyan"), new Color(0xe0ffff));
        put(new CaseInsensitiveString("lightgoldenrodyellow"), new Color(0xfafad2));
        put(new CaseInsensitiveString("lightgray"), new Color(0xd3d3d3));
        put(new CaseInsensitiveString("lightgreen"), new Color(0x90ee90));
        put(new CaseInsensitiveString("lightgrey"), new Color(0xd3d3d3));
        put(new CaseInsensitiveString("lightpink"), new Color(0xffb6c1));
        put(new CaseInsensitiveString("lightsalmon"), new Color(0xffa07a));
        put(new CaseInsensitiveString("lightseagreen"), new Color(0x20b2aa));
        put(new CaseInsensitiveString("lightskyblue"), new Color(0x87cefa));
        put(new CaseInsensitiveString("lightslategray"), new Color(0x778899));
        put(new CaseInsensitiveString("lightslategrey"), new Color(0x778899));
        put(new CaseInsensitiveString("lightsteelblue"), new Color(0xb0c4de));
        put(new CaseInsensitiveString("lightyellow"), new Color(0xffffe0));
        put(new CaseInsensitiveString("lime"), new Color(0x00ff00));
        put(new CaseInsensitiveString("limegreen"), new Color(0x32cd32));
        put(new CaseInsensitiveString("linen"), new Color(0xfaf0e6));
        put(new CaseInsensitiveString("magenta"), new Color(0xff00ff));
        put(new CaseInsensitiveString("maroon"), new Color(0x800000));
        put(new CaseInsensitiveString("mediumaquamarine"), new Color(0x66cdaa));
        put(new CaseInsensitiveString("mediumblue"), new Color(0x0000cd));
        put(new CaseInsensitiveString("mediumorchid"), new Color(0xba55d3));
        put(new CaseInsensitiveString("mediumpurple"), new Color(0x9370db));
        put(new CaseInsensitiveString("mediumseagreen"), new Color(0x3cb371));
        put(new CaseInsensitiveString("mediumslateblue"), new Color(0x7b68ee));
        put(new CaseInsensitiveString("mediumspringgreen"), new Color(0x00fa9a));
        put(new CaseInsensitiveString("mediumturquoise"), new Color(0x48d1cc));
        put(new CaseInsensitiveString("mediumvioletred"), new Color(0xc71585));
        put(new CaseInsensitiveString("midnightblue"), new Color(0x191970));
        put(new CaseInsensitiveString("mintcream"), new Color(0xf5fffa));
        put(new CaseInsensitiveString("mistyrose"), new Color(0xffe4e1));
        put(new CaseInsensitiveString("moccasin"), new Color(0xffe4b5));
        put(new CaseInsensitiveString("navajowhite"), new Color(0xffdead));
        put(new CaseInsensitiveString("navy"), new Color(0x000080));
        put(new CaseInsensitiveString("oldlace"), new Color(0xfdf5e6));
        put(new CaseInsensitiveString("olive"), new Color(0x808000));
        put(new CaseInsensitiveString("olivedrab"), new Color(0x6b8e23));
        put(new CaseInsensitiveString("orange"), new Color(0xffa500));
        put(new CaseInsensitiveString("orangered"), new Color(0xff4500));
        put(new CaseInsensitiveString("orchid"), new Color(0xda70d6));
        put(new CaseInsensitiveString("palegoldenrod"), new Color(0xeee8aa));
        put(new CaseInsensitiveString("palegreen"), new Color(0x98fb98));
        put(new CaseInsensitiveString("paleturquoise"), new Color(0xafeeee));
        put(new CaseInsensitiveString("palevioletred"), new Color(0xdb7093));
        put(new CaseInsensitiveString("papayawhip"), new Color(0xffefd5));
        put(new CaseInsensitiveString("peachpuff"), new Color(0xffdab9));
        put(new CaseInsensitiveString("peru"), new Color(0xcd853f));
        put(new CaseInsensitiveString("pink"), new Color(0xffc0cb));
        put(new CaseInsensitiveString("plum"), new Color(0xdda0dd));
        put(new CaseInsensitiveString("powderblue"), new Color(0xb0e0e6));
        put(new CaseInsensitiveString("purple"), new Color(0x800080));
        put(new CaseInsensitiveString("rebeccapurple"), new Color(0x663399));
        put(new CaseInsensitiveString("red"), new Color(0xff0000));
        put(new CaseInsensitiveString("rosybrown"), new Color(0xbc8f8f));
        put(new CaseInsensitiveString("royalblue"), new Color(0x4169e1));
        put(new CaseInsensitiveString("saddlebrown"), new Color(0x8b4513));
        put(new CaseInsensitiveString("salmon"), new Color(0xfa8072));
        put(new CaseInsensitiveString("sandybrown"), new Color(0xf4a460));
        put(new CaseInsensitiveString("seagreen"), new Color(0x2e8b57));
        put(new CaseInsensitiveString("seashell"), new Color(0xfff5ee));
        put(new CaseInsensitiveString("sienna"), new Color(0xa0522d));
        put(new CaseInsensitiveString("silver"), new Color(0xc0c0c0));
        put(new CaseInsensitiveString("skyblue"), new Color(0x87ceeb));
        put(new CaseInsensitiveString("slateblue"), new Color(0x6a5acd));
        put(new CaseInsensitiveString("slategray"), new Color(0x708090));
        put(new CaseInsensitiveString("slategrey"), new Color(0x708090));
        put(new CaseInsensitiveString("snow"), new Color(0xfffafa));
        put(new CaseInsensitiveString("springgreen"), new Color(0x00ff7f));
        put(new CaseInsensitiveString("steelblue"), new Color(0x4682b4));
        put(new CaseInsensitiveString("tan"), new Color(0xd2b48c));
        put(new CaseInsensitiveString("teal"), new Color(0x008080));
        put(new CaseInsensitiveString("thistle"), new Color(0xd8bfd8));
        put(new CaseInsensitiveString("tomato"), new Color(0xff6347));
        put(new CaseInsensitiveString("turquoise"), new Color(0x40e0d0));
        put(new CaseInsensitiveString("violet"), new Color(0xee82ee));
        put(new CaseInsensitiveString("wheat"), new Color(0xf5deb3));
        put(new CaseInsensitiveString("white"), new Color(0xffffff));
        put(new CaseInsensitiveString("whitesmoke"), new Color(0xf5f5f5));
        put(new CaseInsensitiveString("yellow"), new Color(0xffff00));
        put(new CaseInsensitiveString("yellowgreen"), new Color(0x9acd32));
    }};

    public static int[] getColor(@Nullable String color) {
        if (color == null) return colorless;

        CaseInsensitiveString key = new CaseInsensitiveString(color.replace(" ", ""));
        var colorMatch = colors.get(key);
        if (colorMatch != null) return colorMatch;

        colors.computeIfAbsent(key, key1 -> {
            Color newColor = colorTemplates.get(key1);
            if (newColor != null) {
                return defineColor(newColor);
            } else {
                return colorless;
            }
        });

        for (Map.Entry<CaseInsensitiveString, int[]> entry : colors.entrySet()) {
            if (StringUtils.containsIgnoreCase(color, entry.getKey().toString())) {
                return entry.getValue();
            }
        }

        for (Map.Entry<CaseInsensitiveString, Color> entry : colorTemplates.entrySet()) {
            if (StringUtils.containsIgnoreCase(color, entry.getKey().toString())) {
                var newColor = defineColor(entry.getValue());
                colors.put(entry.getKey(), newColor);
                return newColor;
            }
        }

        return colorless;
    }

    private static int @NotNull [] defineColor(Color newColor) {
        int[] newTexture = new int[colorless.length];

        for (int i = 0; i < colorless.length; i++) {
            if (colorless[i] != -1) {
                var r = Math.round((((colorless[i] >> 16) & 0xff)/255.0f)*(newColor.getRed()/255.0f)*255);
                var g = Math.round((((colorless[i] >> 8) & 0xff)/255.0f)*(newColor.getGreen()/255.0f)*255);
                var b = Math.round((((colorless[i] >> 0) & 0xff)/255.0f)*(newColor.getBlue()/255.0f)*255);
                var a = Math.round((((colorless[i] >> 24) & 0xff)/255.0f)*(newColor.getAlpha()/255.0f)*255);
                newTexture[i] = r << 16 | g << 8 | b << 0 | a << 24;
            } else {
                newTexture[i] = -1;
            }
        }

        return newTexture;
    }
}
