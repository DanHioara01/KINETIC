const fs = require('fs');
const path = 'app/src/main/java/com/example/kinetic/GpsCardioScreen.kt';
let content = fs.readFileSync(path, 'utf8');

// FIX 1: Replace MAPNIK with CartoDB Dark_all tiles (free, no 403, matches dark theme)
// CartoDB dark_all: https://basemaps.cartocdn.com/dark_all/{z}/{x}/{y}@2x.png
const oldTileSource = 'setTileSource(TileSourceFactory.MAPNIK)';
const newTileSource = `setTileSource(org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase(
                    "CartoDB_Dark",
                    0, 20, 256, ".png",
                    arrayOf(
                        "https://a.basemaps.cartocdn.com/dark_all/",
                        "https://b.basemaps.cartocdn.com/dark_all/",
                        "https://c.basemaps.cartocdn.com/dark_all/"
                    ),
                    "KineticGPS/1.0 (Android; fitness-app) {map}"
                ))`;

content = content.replace(oldTileSource, newTileSource);

fs.writeFileSync(path, content, 'utf8');
console.log('Map tile source changed from MAPNIK to CartoDB Dark');
