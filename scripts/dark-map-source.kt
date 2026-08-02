// Concrete tile source for CartoDB Dark map (avoids 403 from MAPNIK)
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
        val zoom = org.osmdroid.util.MapView.getZoomForByteArray(pMapTileIndex).toInt()
        val n = 1 shl (20 - zoom)
        val x = (pMapTileIndex shr 32).toInt()
        val y = (pMapTileIndex and 0xFFFFFFFFL).toInt()
        return baseURLs[randomInt % baseURLs.length] + zoom + "/" + x + "/" + y + imageFilenameEnding
    }
}