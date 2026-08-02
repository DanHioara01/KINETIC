const fs = require('fs');
const path = 'app/src/main/java/com/example/kinetic/GpsCardioScreen.kt';
let content = fs.readFileSync(path, 'utf8');

// FIX: Replace the abstract OnlineTileSourceBase instantiation with a concrete subclass
const oldTileSource = `setTileSource(org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase(
                    "CartoDB_Dark",
                    0, 20, 256, ".png",
                    arrayOf(
                        "https://a.basemaps.cartocdn.com/dark_all/",
                        "https://b.basemaps.cartocdn.com/dark_all/",
                        "https://c.basemaps.cartocdn.com/dark_all/"
                    ),
                    "KineticGPS/1.0 (Android; fitness-app) {map}"
                ))`;

const newTileSource = `setTileSource(DarkMapTileSource)`;

// FIX 2: Add a concrete tile source class before the RouteCanvas function
const routeCanvasMarker = 'private fun RouteCanvas(';

const darkMapTileSource = `// Concrete tile source for CartoDB Dark map (avoids 403 from MAPNIK)
private object DarkMapTileSource : org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase(
    "CartoDB_Dark",
    0, 20, 256, ".png",
    arrayOf(
        "https://a.basemaps.cartocdn.com/dark_all/",
        "https://b.basemaps.cartocdn.com/dark_all/",
        "https://c.basemaps.cartocdn.com/dark_all/"
    )
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = org.osmdroid.util.MapView.getZoomForByteArray(pMapTileIndex).toInt() // not needed, use built-in
        val n = 1 shl (20 - org.osmdroid.util.MapView.getZoomForByteArray(pMapTileIndex).toInt())
        val x = (pMapTileIndex shr 32).toInt()
        val y = (pMapTileIndex and 0xFFFFFFFFL).toInt()
        return baseURLs[randomInt % baseURLs.length] + org.osmdroid.util.MapView.getZoomForByteArray(pMapTileIndex).toInt() + "/" + x + "/" + y + imageFilenameEnding
    }
}

`;

// Apply changes
content = content.replace(routeCanvasMarker, darkMapTileSource + routeCanvasMarker);
content = content.replace(oldTileSource, newTileSource);

fs.writeFileSync(path, content, 'utf8');
console.log('Fixed: replaced abstract OnlineTileSourceBase with concrete DarkMapTileSource object');
