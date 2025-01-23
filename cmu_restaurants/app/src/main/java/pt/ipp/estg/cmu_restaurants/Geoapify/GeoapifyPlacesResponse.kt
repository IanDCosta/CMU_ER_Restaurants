package Models.Geoapify

data class GeoapifyPlacesResponse(
    val features: List<PlaceFeature>
)

data class PlaceFeature(
    val properties: PlaceProperties
)

data class PlaceProperties(
    val name: String?,
    val lat: Double,
    val lon: Double,
    val formatted: String?,
    val distance: Int?
)
