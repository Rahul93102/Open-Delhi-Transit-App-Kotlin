package com.example.opendelhitransit.features.steptracker

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.opendelhitransit.R
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CountStepsActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometerSensor: Sensor
    private lateinit var magnetometerSensor: Sensor
    private lateinit var btnStopSensor: Button
    private lateinit var tvDirection: TextView
    private lateinit var tvStrideLength: TextView
    private lateinit var tvDisplacement: TextView
    private var accelerometerSpike = 0
    private var zaccelerometerSpike = 0
    private var start: Double = 0.0
    private var isOriginSet: Boolean = false
    private var totalSteps = 0
    private var currEWMA = 0.0
    private var currEWMAforZ = 0.0
    private var height: Int = 170
    private var strideLength: Double = 70.0
    private var xDisplacement: Double = 0.0
    private var yDisplacement: Double = 0.0
    private var isSensorsActive = false
    private var isButtonClicked = false
    private lateinit var mScaleGestureDetector: ScaleGestureDetector
    private var mScaleFactor = 1.0f
    private val accelerometerValues = FloatArray(3)
    private val magneticSensorValues = FloatArray(3)
    private lateinit var canvasView: CanvasView
    private var currDisplacement: Double = 0.0
    private var currAzimuth = 0.0

    private lateinit var tvStepCount: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_count_steps)

        tvStepCount = findViewById(R.id.tv_step_count)
        tvDirection = findViewById(R.id.tv_direction)
        tvStrideLength = findViewById(R.id.tv_stride)
        tvDisplacement = findViewById(R.id.tv_displacement)
        canvasView = findViewById(R.id.canvas_view)
        mScaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
        height = intent.getIntExtra("height", 170)
        strideLength = calculateStrideLength(height.toDouble())
        tvStrideLength.text = "Stride Length: ${strideLength.toInt()} cm"
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: throw IllegalStateException("Accelerometer sensor not available")
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) ?: throw IllegalStateException("Magnetometer sensor not available")
        btnStopSensor = findViewById(R.id.btn_stop_sensor)
        btnStopSensor.setOnClickListener {
            toggleSensorListener()
            isButtonClicked = true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun toggleSensorListener() {
        if (isSensorsActive) {
            if(accelerometerSpike >= 10){
                totalSteps += 1
                val dx = cos(currAzimuth - start)*(strideLength/100.0)
                val dy = sin(currAzimuth - start)*(strideLength/100.0)
                canvasView.updateData(dx, dy, false)
                xDisplacement += dx
                yDisplacement += dy
                Log.i("CountSteps", "start: $start, xDisplacement : $xDisplacement, yDisplacement : $yDisplacement, dx : $dx, dy = $dy")
                currDisplacement = sqrt(xDisplacement*xDisplacement + yDisplacement*yDisplacement)/100.0
                val formatted = String.format("%.8f", currDisplacement)
                tvDisplacement.text = "Displacement: $formatted metres"
            }
            isOriginSet = false
            btnStopSensor.text = "Start Walking"
            btnStopSensor.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            sensorManager.unregisterListener(this)
        } else {
            btnStopSensor.text = "Stop Walking"
            accelerometerSpike = 0
            totalSteps = 0
            xDisplacement = 0.0
            yDisplacement = 0.0
            tvStepCount.text = "Steps Count: $totalSteps"
            val formatted = String.format("%.8f", currDisplacement)
            canvasView.updateData((canvasView.width/2).toDouble(), (canvasView.height/2).toDouble(), true)
            tvDisplacement.text = "Displacement: $formatted metres"
            btnStopSensor.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_GAME)
        }
        isSensorsActive = !isSensorsActive
    }

    override fun onResume() {
        super.onResume()
        if (isButtonClicked && isSensorsActive) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isButtonClicked) {
            sensorManager.unregisterListener(this)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelerometerValues[0] = event.values[0]
                accelerometerValues[1] = event.values[1]
                accelerometerValues[2] = event.values[2]

                // FOR STEPS COUNT
                val magnitudeAcceleration = sqrt(accelerometerValues[0]*accelerometerValues[0] + accelerometerValues[1]*accelerometerValues[1] + (accelerometerValues[2]-9.81f)*(accelerometerValues[2]-9.81f))
                currEWMA = calculateEWMA(magnitudeAcceleration, currEWMA, 0.08f)
                if(magnitudeAcceleration > currEWMA+1.0){
                    accelerometerSpike+=1
                    if(accelerometerSpike >= 10){
                        totalSteps += 1
                        tvStepCount.text = "Steps Count: $totalSteps"
                        val dx = cos(currAzimuth - start)*(strideLength/100.0)
                        val dy = sin(currAzimuth - start)*(strideLength/100.0)
                        canvasView.updateData(dx, dy, false)
                        xDisplacement += dx
                        yDisplacement += dy
                        Log.i("CountSteps", "start: $start, xDisplacement : $xDisplacement, yDisplacement : $yDisplacement, dx : $dx, dy = $dy")
                        currDisplacement = sqrt(xDisplacement*xDisplacement + yDisplacement*yDisplacement)/100.0
                        val formatted = String.format("%.8f", currDisplacement)
                        tvDisplacement.text = "Displacement: $formatted metres"
                        accelerometerSpike = 0
                    }
                }

                // FOR STAIRS DETECTION
                val zAcceleration = event.values[2]
                currEWMAforZ = calculateEWMA(zAcceleration, currEWMAforZ, 0.3f)
                if(zAcceleration > currEWMAforZ+6){
                    zaccelerometerSpike+=1
                    if(zaccelerometerSpike >= 7){
                        Toast.makeText(this, "Using Stairs", Toast.LENGTH_SHORT).show()
                        zaccelerometerSpike = 0
                    }
                }
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                magneticSensorValues[0] = event.values[0]
                magneticSensorValues[1] = event.values[1]
                magneticSensorValues[2] = event.values[2]

                // FOR LIFT DETECTION
                val magnitudeMagnetometer = sqrt(magneticSensorValues[0]*magneticSensorValues[0] + magneticSensorValues[1]*magneticSensorValues[1] + magneticSensorValues[2]*magneticSensorValues[2])
                if(magnitudeMagnetometer <= 27.5){
                    Toast.makeText(this, "Using Lift", Toast.LENGTH_SHORT).show()
                }

                // FOR DIRECTION DETECTION
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValues, magneticSensorValues)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val xToDegree = orientation[0] * 180 / Math.PI
                setDirection(xToDegree)
                currAzimuth = orientation[0].toDouble()
                if(!isOriginSet){
                    start = currAzimuth
                    isOriginSet = true
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    @SuppressLint("SetTextI18n")
    private fun setDirection(degree: Double) {
        var dir = "North"
        if(degree >= -22.5 && degree < 22.5) dir = "North"
        else if(degree >= 22.5 && degree < 67.5) dir = "North-East"
        else if(degree >= 67.5 && degree < 112.5) dir = "East"
        else if(degree >= 112.5 && degree < 157.5) dir = "South-East"
        else if((degree >= 157.5 && degree <= 180) || (degree >= -180 && degree < -157.5)) dir = "South"
        else if(degree >= -157.5 && degree < -112.5) dir = "South-West"
        else if(degree >= -112.5 && degree < -67.5) dir = "West"
        else if(degree >= -67.5 && degree < -22.5) dir = "North-West"

        tvDirection.text = "Direction: $dir"
    }

    private fun calculateStrideLength(height: Double): Double {
        return height * 0.415
    }

    private fun calculateEWMA(value: Float, currAvg: Double, weight: Float): Double {
        return weight*value + (1-weight)*currAvg
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { mScaleGestureDetector.onTouchEvent(it) }
        return true
    }

    inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f))
            canvasView.setScaleFactor(mScaleFactor)
            canvasView.invalidate()
            return true
        }
    }
} 