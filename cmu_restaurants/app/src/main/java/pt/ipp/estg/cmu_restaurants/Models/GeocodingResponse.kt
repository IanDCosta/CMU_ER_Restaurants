package pt.ipp.estg.cmu_restaurants.Models

data class GeocodingResponse(
    val features: List<Feature>
)

data class Feature(
    val geometry: Geometry
)

data class Geometry(
    val coordinates: List<Double> // [longitude, latitude]
)
