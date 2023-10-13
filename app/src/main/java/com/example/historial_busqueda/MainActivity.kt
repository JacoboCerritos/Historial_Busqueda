package com.example.historial_busqueda

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Scanner

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.busqueda_1)

        val saveLocationButton: Button = findViewById(R.id.saveLocationButton)
        val NameLocationEdit: EditText = findViewById(R.id.EditText)
        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Manejar el clic en el botón
        saveLocationButton.setOnClickListener {
            if (checkLocationPermission()) {
                saveLocation()
            } else {
                requestLocationPermission()
            }
        }

        val locationsListView: ListView = findViewById(R.id.locationsListView)
        val locations = readLocationsFromFile()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, locations)
        locationsListView.adapter = adapter



    }


    private fun checkLocationPermission(): Boolean {
        val permission1 = Manifest.permission.ACCESS_FINE_LOCATION
        val permission2 = Manifest.permission.ACCESS_COARSE_LOCATION
        val result1 = ContextCompat.checkSelfPermission(this, permission1)
        val result2 = ContextCompat.checkSelfPermission(this, permission2)
        return result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE)
    }

    private fun saveLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    // Guardar la ubicación en la base de datos o archivo
                    saveLocationToFile(it)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error obteniendo la ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveLocationToFile(location: Location) {

    }


    private fun showLocationNameDialog(location: Location) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ingrese un nombre para esta ubicación")

        val input = EditText (this)
        builder.setView(input)

        builder.setPositiveButton("Guardar") { _, _ ->
            val locationName = input.text.toString()
            saveLocationToFile(location, locationName)
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun saveLocationToFile(location: Location, locationName: String) {
        val fileName = "ubicaciones.txt"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

        try {
            val writer = FileWriter(file, true) // true para modo de apertura en añadir
            val latitude = location.latitude
            val longitude = location.longitude
            writer.append("Nombre: $locationName, Latitud: $latitude, Longitud: $longitude\n")
            writer.flush()
            writer.close()
            Toast.makeText(this, "Ubicación guardada en $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, "Error al guardar la ubicación", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso de ubicación concedido, ahora puedes guardar la ubicación
                saveLocation()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun readLocationsFromFile(): ArrayList<String> {
        val locations = ArrayList<String>()
        val fileName = "ubicaciones.txt"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

        try {
            val scanner = Scanner(file)
            while (scanner.hasNextLine()) {
                val locationLine = scanner.nextLine()
                locations.add(locationLine)
            }
            scanner.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return locations
    }




}

