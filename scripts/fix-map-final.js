const fs = require('fs');
const filePath = 'app/src/main/java/com/example/kinetic/GpsCardioScreen.kt';
let content = fs.readFileSync(filePath, 'utf8');

// STEP 1: Remove the broken abstract OnlineTileSourceBase instantiation
const brokenSource = `setTileSource(DarkMapTileSource)`;
const fixedSource = `setTileSource(cartoDarkSource())`;
content = content.replace(brokenSource, fixedSource);

// Also handle if it still has the abstract instantiation
const abstractSource = /setTileSource\(org\.osmdroid\.tileprovider\.tilesource\.OnlineTileSourceBase\([\s\S]*?\)\)/;
if (abstractSource.test(content)) {
    content = content.replace(abstractSource, 'setTileSource(cartoDarkSource())');
}

// Remove any stale DarkMapTileSource object definitions
const staleObj = /\/\/ Concrete tile source for CartoDB Dark.*?private object DarkMapTileSource[\s\S]*?}\n\n/g;
content = content.replace(staleObj, '');

// STEP 2: Add a helper function that creates the tile source using a proper subclass
const routeCanvasMarker = 'private fun RouteCanvas(';

const tileSourceHelper = `// Helper: CartoDB Dark tile source (free, no 403, matches dark theme)
private fun cartoDarkSource(): org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase {
    return object : org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase(
        "CartoDB_Dark", 0, 20, 256, ".png",
        arrayOf(
            "https://a.basemaps.cartocdn.com/dark_all/",
            "https://b.basemaps.cartocdn.com/dark_all/",
            "https://c.basemaps.cartocdn.com/dark_all/"
        )
    ) {
        override fun getTileURLString(pMapTileIndex: Long): String {
            val zoom = org.osmdroid.util.MapView.getZoomForByteArray(pMapTileIndex).toInt()
            val n = 1 shl (20 - zoom)
            val x = (pMapTileIndex shr 32).toInt()
            val y = (pMapTileIndex and 0xFFFFFFFFL).toInt()
            val servers = arrayOf("a", "b", "c")
            val server = servers[Math.abs(pMapTileIndex.toInt()) % servers.size]
            return "https://\${server}.basemaps.cartocdn.com/dark_all/\$zoom/\$x/\$y.png"
        }
    }
}

`;

if (!content.includes('cartoDarkSource')) {
    content = content.replace(routeCanvasMarker, tileSourceHelper + routeCanvasMarker);
}

fs.writeFileSync(filePath, content, 'utf8');
console.log('Applied CartoDB Dark tile source fix');
