package mini.app.orbis;

/**
 * Created by Jan on 04/12/2016.
 */

public class GuideManager {

    public static Guide[] guides = {
            new Guide("Sample Guide", R.drawable.orbis_logo, "file:///android_asset/guides/sample.html"),
            new Guide("3D Photography", R.drawable.orbis_logo, "file:///android_asset/guides/sample.html"),
            new Guide("Getting Started", R.drawable.orbis_logo, "file:///android_asset/guides/sample.html"),
            new Guide("About Orbis VR", R.drawable.orbis_logo, "file:///android_asset/guides/sample.html")
    };

    static class Guide {
        private String title, htmlResourcePath;
        private int titleImageResourceID;
        Guide(String title, int titleImageResourceID, String htmlResourcePath) {
            this.title = title;
            this.titleImageResourceID = titleImageResourceID;
            this.htmlResourcePath = htmlResourcePath;
        }
        public String getGuideTitle() {
            return title;
        }
        public void setGuideTitle(String title) {
            this.title = title;
        }
        public String getHtmlResourcePath() {
            return htmlResourcePath;
        }
        public void setHtmlResourcePath(String htmlResourcePath) {
            this.htmlResourcePath = htmlResourcePath;
        }
        public int getTitleImageResourceID() {
            return titleImageResourceID;
        }
        public void setTitleImageResourceID(int titleImageResourceID) {
            this.titleImageResourceID = titleImageResourceID;
        }
    }

}
