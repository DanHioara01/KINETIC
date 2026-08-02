const fs = require('fs');
const path = 'app/src/main/java/com/example/kinetic/GpsCardioScreen.kt';
let content = fs.readFileSync(path, 'utf8');

// Find the old RouteCanvas function boundaries
const startMarker = 'private fun RouteCanvas(';
const endMarker = 'private fun createCircleBitmap(';

const startIdx = content.indexOf(startMarker);
const endIdx = content.indexOf(endMarker);

if (startIdx === -1 || endIdx === -1) {
    console.log('ERROR: Could not find function boundaries');
    process.exit(1);
}

// Find the @Composable annotation before RouteCanvas
let annotStart = content.lastIndexOf('@Composable', startIdx);

const newRouteCanvas = `@Composable
private fun RouteCanvas(
    points: List<GpsPoint>,
    isTracking: Boolean,
    lastLocation: Location?,
    bearing: Float = 0f,
    forceCenterOnLocation: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var lastPointsHash by remember { mutableIntStateOf(0) }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(false)
                zoomController.setVisibility(org.osmdroid.views.overlay.controls.ZoomController.Visibility.NONE)
                setBackgroundColor(android.graphics.Color.BLACK)

                val centerLat = lastLocation?.latitude ?: 44.4268
                val centerLng = lastLocation?.longitude ?: 26.1025
                controller.setZoom(16.0)
                controller.setCenter(GeoPoint(centerLat, centerLng))

                if (points.size >= 2) {
                    val routeLine = Polyline()
                    routeLine.outlinePaint.color = android.graphics.Color.parseColor("#ff2d2d")
                    routeLine.outlinePaint.strokeWidth = 8f
                    points.forEach { routeLine.addPoint(GeoPoint(it.lat, it.lng)) }
                    overlays.add(routeLine)
                }
                if (points.isNotEmpty() && !isTracking) {
                    val sm = Marker(this)
                    sm.position = GeoPoint(points.first().lat, points.first().lng)
                    sm.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    sm.setIcon(makeCircleDrawable(ctx, android.graphics.Color.parseColor("#4CAF50"), 16))
                    overlays.add(sm)
                }
                if (points.isNotEmpty() && !isTracking) {
                    val em = Marker(this)
                    em.position = GeoPoint(points.last().lat, points.last().lng)
                    em.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    em.setIcon(makeCircleDrawable(ctx, android.graphics.Color.parseColor("#ff2d2d"), 16))
                    overlays.add(em)
                }
                if (lastLocation != null) {
                    val cm = Marker(this)
                    cm.position = GeoPoint(lastLocation.latitude, lastLocation.longitude)
                    cm.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    cm.setIcon(makeCircleDrawable(ctx, android.graphics.Color.parseColor("#007aff"), 12))
                    overlays.add(cm)
                }
                if (!isTracking && points.size >= 2) {
                    val geoPts = points.map { GeoPoint(it.lat, it.lng) }
                    val bounds = org.osmdroid.util.BoundingBox.fromGeoPoints(geoPts)
                    zoomToBoundingBox(bounds.increaseByScale(1.3f), false)
                } else if (isTracking && lastLocation != null) {
                    controller.animateTo(GeoPoint(lastLocation.latitude, lastLocation.longitude))
                    controller.setZoom(17.0)
                }
                invalidate()
            }
        },
        update = { osmMap ->
            val centerLat = lastLocation?.latitude ?: 44.4268
            val centerLng = lastLocation?.longitude ?: 26.1025
            val newHash = points.hashCode() + centerLat.hashCode() + centerLng.hashCode() + isTracking.hashCode() + bearing.hashCode()
            if (newHash != lastPointsHash) {
                lastPointsHash = newHash
                osmMap.overlays.clear()
                if (points.size >= 2) {
                    val routeLine = Polyline()
                    routeLine.outlinePaint.color = android.graphics.Color.parseColor("#ff2d2d")
                    routeLine.outlinePaint.strokeWidth = 8f
                    points.forEach { routeLine.addPoint(GeoPoint(it.lat, it.lng)) }
                    osmMap.overlays.add(routeLine)
                }
                if (points.isNotEmpty() && !isTracking) {
                    val sm = Marker(osmMap)
                    sm.position = GeoPoint(points.first().lat, points.first().lng)
                    sm.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    sm.setIcon(makeCircleDrawable(osmMap.context, android.graphics.Color.parseColor("#4CAF50"), 16))
                    osmMap.overlays.add(sm)
                }
                if (points.isNotEmpty() && !isTracking) {
                    val em = Marker(osmMap)
                    em.position = GeoPoint(points.last().lat, points.last().lng)
                    em.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    em.setIcon(makeCircleDrawable(osmMap.context, android.graphics.Color.parseColor("#ff2d2d"), 16))
                    osmMap.overlays.add(em)
                }
                if (lastLocation != null) {
                    val cm = Marker(osmMap)
                    cm.position = GeoPoint(lastLocation.latitude, lastLocation.longitude)
                    cm.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    cm.setIcon(makeCircleDrawable(osmMap.context, android.graphics.Color.parseColor("#007aff"), 12))
                    osmMap.overlays.add(cm)
                }
                if (isTracking && lastLocation != null) {
                    osmMap.controller.animateTo(GeoPoint(lastLocation.latitude, lastLocation.longitude))
                } else if (!isTracking && points.size >= 2) {
                    val geoPts = points.map { GeoPoint(it.lat, it.lng) }
                    val bounds = org.osmdroid.util.BoundingBox.fromGeoPoints(geoPts)
                    osmMap.zoomToBoundingBox(bounds.increaseByScale(1.3f), false)
                } else if (lastLocation != null) {
                    osmMap.controller.setCenter(GeoPoint(lastLocation.latitude, lastLocation.longitude))
                }
                osmMap.invalidate()
            }
        },
        modifier = modifier
    )
}

private fun makeCircleDrawable(ctx: android.content.Context, color: Int, sizeDp: Int): android.graphics.drawable.Drawable {
    val density = ctx.resources.displayMetrics.density
    val sizePx = (sizeDp * density).toInt().coerceAtLeast(1)
    val bitmap = android.graphics.Bitmap.createBitmap(sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint().apply {
        this.color = color
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, paint)
    paint.color = android.graphics.Color.WHITE
    paint.strokeWidth = sizePx / 8f
    paint.style = android.graphics.Paint.Style.STROKE
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2.5f, paint)
    return android.graphics.drawable.BitmapDrawable(ctx.resources, bitmap)
}

`;

content = content.slice(0, annotStart) + newRouteCanvas + content.slice(endIdx);

fs.writeFileSync(path, content, 'utf8');
console.log('SUCCESS: Replaced WebView RouteCanvas with OSMDroid MapView');
