package com.example.opendelhitransit.util

import android.util.Log
import okhttp3.ResponseBody
import java.nio.ByteBuffer
import java.nio.ByteOrder

object GtfsRtUtil {
    private const val TAG = "GtfsRtUtil"
    private const val WIRE_TYPE_VARINT = 0
    private const val WIRE_TYPE_64BIT = 1
    private const val WIRE_TYPE_LENGTH_DELIMITED = 2
    private const val WIRE_TYPE_32BIT = 5
    private const val ENTITY_LIST_FIELD = 2L
    private const val VEHICLE_FIELD = 4L
    private const val VEHICLE_ID_FIELD = 1L
    private const val VEHICLE_LABEL_FIELD = 2L
    private const val TRIP_FIELD = 1L
    private const val TRIP_ROUTE_ID_FIELD = 5L
    private const val TRIP_ID_FIELD = 1L
    private const val POSITION_FIELD = 2L
    private const val POSITION_LATITUDE_FIELD = 1L
    private const val POSITION_LONGITUDE_FIELD = 2L
    private const val POSITION_BEARING_FIELD = 3L
    private const val POSITION_SPEED_FIELD = 5L
    private const val TIMESTAMP_FIELD = 5L

    fun parseVehiclePositions(responseBody: ResponseBody): List<BusLocation> {
        val busLocations = mutableListOf<BusLocation>()
        try {
            val bytes = responseBody.bytes()
            var position = 0
            while (position < bytes.size) {
                val tag = readVarint(bytes, position)
                position += getVarintSize(tag)
                val fieldId = tag shr 3
                val wireType = (tag and 0x7).toInt()
                if (fieldId == ENTITY_LIST_FIELD && wireType == WIRE_TYPE_LENGTH_DELIMITED) {
                    val length = readVarint(bytes, position).toInt()
                    position += getVarintSize(readVarint(bytes, position))
                    val entityBytes = bytes.copyOfRange(position, position + length)
                    val busLocation = parseEntity(entityBytes)
                    if (busLocation != null) {
                        busLocations.add(busLocation)
                    }
                    position += length
                } else {
                    position = skipField(bytes, position, wireType)
                }
            }
            Log.d(TAG, "Parsed ${busLocations.size} bus locations")
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing protocol buffer data: ${e.message}")
            e.printStackTrace()
        }
        return busLocations
    }

    private fun parseEntity(bytes: ByteArray): BusLocation? {
        try {
            var vehicleId = ""
            var label = ""
            var routeId = ""
            var tripId = ""
            var latitude = 0.0
            var longitude = 0.0
            var speed = 0f
            var timestamp = 0L
            var position = 0
            while (position < bytes.size) {
                val tag = readVarint(bytes, position)
                position += getVarintSize(tag)
                val fieldId = tag shr 3
                val wireType = (tag and 0x7).toInt()
                if (fieldId == VEHICLE_FIELD && wireType == WIRE_TYPE_LENGTH_DELIMITED) {
                    val length = readVarint(bytes, position).toInt()
                    position += getVarintSize(readVarint(bytes, position))
                    val vehicleBytes = bytes.copyOfRange(position, position + length)
                    val result = parseVehicle(vehicleBytes)
                    vehicleId = result["vehicleId"] as String? ?: ""
                    label = result["label"] as String? ?: ""
                    routeId = result["routeId"] as String? ?: ""
                    tripId = result["tripId"] as String? ?: ""
                    latitude = result["latitude"] as Double? ?: 0.0
                    longitude = result["longitude"] as Double? ?: 0.0
                    speed = result["speed"] as Float? ?: 0f
                    timestamp = result["timestamp"] as Long? ?: 0L
                    position += length
                } else {
                    position = skipField(bytes, position, wireType)
                }
            }
            if (vehicleId.isNotEmpty() && latitude != 0.0 && longitude != 0.0) {
                return BusLocation(
                    vehicleId = vehicleId,
                    label = label,
                    routeId = routeId,
                    tripId = tripId,
                    latitude = latitude,
                    longitude = longitude,
                    speed = speed,
                    timestamp = timestamp
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing entity: ${e.message}")
        }
        return null
    }

    private fun parseVehicle(bytes: ByteArray): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        var position = 0
        while (position < bytes.size) {
            val tag = readVarint(bytes, position)
            position += getVarintSize(tag)
            val fieldId = tag shr 3
            val wireType = (tag and 0x7).toInt()
            when (fieldId) {
                TRIP_FIELD -> {
                    if (wireType == WIRE_TYPE_LENGTH_DELIMITED) {
                        val length = readVarint(bytes, position).toInt()
                        position += getVarintSize(readVarint(bytes, position))
                        val tripBytes = bytes.copyOfRange(position, position + length)
                        val tripResult = parseTripInfo(tripBytes)
                        result.putAll(tripResult)
                        position += length
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                POSITION_FIELD -> {
                    if (wireType == WIRE_TYPE_LENGTH_DELIMITED) {
                        val length = readVarint(bytes, position).toInt()
                        position += getVarintSize(readVarint(bytes, position))
                        val posBytes = bytes.copyOfRange(position, position + length)
                        val posResult = parsePositionInfo(posBytes)
                        result.putAll(posResult)
                        position += length
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                8L -> {
                    if (wireType == WIRE_TYPE_LENGTH_DELIMITED) {
                        val length = readVarint(bytes, position).toInt()
                        position += getVarintSize(readVarint(bytes, position))
                        val vehBytes = bytes.copyOfRange(position, position + length)
                        val vehResult = parseVehicleInfo(vehBytes)
                        result.putAll(vehResult)
                        position += length
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                TIMESTAMP_FIELD -> {
                    if (wireType == WIRE_TYPE_VARINT) {
                        val timestamp = readVarint(bytes, position)
                        position += getVarintSize(timestamp)
                        result["timestamp"] = timestamp
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                else -> {
                    position = skipField(bytes, position, wireType)
                }
            }
        }
        return result
    }

    private fun parseTripInfo(bytes: ByteArray): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        var position = 0
        while (position < bytes.size) {
            val tag = readVarint(bytes, position)
            position += getVarintSize(tag)
            val fieldId = tag shr 3
            val wireType = (tag and 0x7).toInt()
            when (fieldId) {
                TRIP_ROUTE_ID_FIELD -> {
                    if (wireType == WIRE_TYPE_LENGTH_DELIMITED) {
                        val length = readVarint(bytes, position).toInt()
                        position += getVarintSize(readVarint(bytes, position))
                        val routeId = String(bytes, position, length)
                        result["routeId"] = routeId
                        position += length
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                TRIP_ID_FIELD -> {
                    if (wireType == WIRE_TYPE_LENGTH_DELIMITED) {
                        val length = readVarint(bytes, position).toInt()
                        position += getVarintSize(readVarint(bytes, position))
                        val tripId = String(bytes, position, length)
                        result["tripId"] = tripId
                        position += length
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                else -> {
                    position = skipField(bytes, position, wireType)
                }
            }
        }
        return result
    }

    private fun parsePositionInfo(bytes: ByteArray): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        var position = 0
        while (position < bytes.size) {
            val tag = readVarint(bytes, position)
            position += getVarintSize(tag)
            val fieldId = tag shr 3
            val wireType = (tag and 0x7).toInt()
            when (fieldId) {
                POSITION_LATITUDE_FIELD -> {
                    if (wireType == WIRE_TYPE_32BIT) {
                        val buffer = ByteBuffer.wrap(bytes, position, 4).order(ByteOrder.LITTLE_ENDIAN)
                        val latitude = buffer.float.toDouble()
                        result["latitude"] = latitude
                        position += 4
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                POSITION_LONGITUDE_FIELD -> {
                    if (wireType == WIRE_TYPE_32BIT) {
                        val buffer = ByteBuffer.wrap(bytes, position, 4).order(ByteOrder.LITTLE_ENDIAN)
                        val longitude = buffer.float.toDouble()
                        result["longitude"] = longitude
                        position += 4
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                POSITION_SPEED_FIELD -> {
                    if (wireType == WIRE_TYPE_32BIT) {
                        val buffer = ByteBuffer.wrap(bytes, position, 4).order(ByteOrder.LITTLE_ENDIAN)
                        val speed = buffer.float
                        result["speed"] = speed
                        position += 4
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                else -> {
                    position = skipField(bytes, position, wireType)
                }
            }
        }
        return result
    }

    private fun parseVehicleInfo(bytes: ByteArray): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        var position = 0
        while (position < bytes.size) {
            val tag = readVarint(bytes, position)
            position += getVarintSize(tag)
            val fieldId = tag shr 3
            val wireType = (tag and 0x7).toInt()
            when (fieldId) {
                VEHICLE_ID_FIELD -> {
                    if (wireType == WIRE_TYPE_LENGTH_DELIMITED) {
                        val length = readVarint(bytes, position).toInt()
                        position += getVarintSize(readVarint(bytes, position))
                        val vehicleId = String(bytes, position, length)
                        result["vehicleId"] = vehicleId
                        position += length
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                VEHICLE_LABEL_FIELD -> {
                    if (wireType == WIRE_TYPE_LENGTH_DELIMITED) {
                        val length = readVarint(bytes, position).toInt()
                        position += getVarintSize(readVarint(bytes, position))
                        val label = String(bytes, position, length)
                        result["label"] = label
                        position += length
                    } else {
                        position = skipField(bytes, position, wireType)
                    }
                }
                else -> {
                    position = skipField(bytes, position, wireType)
                }
            }
        }
        return result
    }

    private fun skipField(bytes: ByteArray, position: Int, wireType: Int): Int {
        var newPosition = position
        when (wireType) {
            WIRE_TYPE_VARINT -> {
                val value = readVarint(bytes, newPosition)
                newPosition += getVarintSize(value)
            }
            WIRE_TYPE_64BIT -> {
                newPosition += 8
            }
            WIRE_TYPE_LENGTH_DELIMITED -> {
                val length = readVarint(bytes, newPosition).toInt()
                newPosition += getVarintSize(readVarint(bytes, newPosition))
                newPosition += length
            }
            WIRE_TYPE_32BIT -> {
                newPosition += 4
            }
        }
        return newPosition
    }

    private fun readVarint(bytes: ByteArray, position: Int): Long {
        var result: Long = 0
        var shift = 0
        var i = position
        while (i < bytes.size) {
            val b = bytes[i].toLong() and 0xFF
            result = result or ((b and 0x7F) shl shift)
            if (b and 0x80 == 0L) {
                break
            }
            shift += 7
            i++
        }
        return result
    }

    private fun getVarintSize(value: Long): Int {
        var size = 0
        var v = value
        do {
            v = v shr 7
            size++
        } while (v != 0L)
        return size
    }
}
