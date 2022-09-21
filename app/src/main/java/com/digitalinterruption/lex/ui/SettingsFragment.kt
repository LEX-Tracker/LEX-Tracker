package com.digitalinterruption.lex.ui

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.digitalinterruption.lex.R
import com.digitalinterruption.lex.SharedPrefs
import com.digitalinterruption.lex.databinding.FragmentSettingsBinding
import com.digitalinterruption.lex.models.MyViewModel
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.io.FileReader
import java.io.FileWriter


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding
    private lateinit var callback: OnBackPressedCallback
    var exit: Boolean = false
    lateinit var countDown: CountDownTimer
    val myViewModel: MyViewModel by viewModels()
    var isExport: Boolean = false
    lateinit var prefs: SharedPrefs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        prefs = SharedPrefs(requireContext())
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentBackPress()

        binding?.switchOwp?.isChecked = prefs.getOvulationEnabled()
        binding?.switchPms?.isChecked = prefs.getPmsEnabled()

        binding?.ivHome?.setOnClickListener {
            moveToNext("Home")
        }
        binding?.ivSymptoms?.setOnClickListener {
            moveToNext("Symptoms")
        }
        binding?.ivSettings?.setOnClickListener {
        }

        binding?.exportLayout?.setOnClickListener {
            isExport = true
            if (checkPermission()) {
                exportCSV()
            } else {
                requestReadWritePermission()
            }
        }

        binding?.importLayout?.setOnClickListener {
            isExport = false
            if (checkPermission()) {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "text/csv"
                startActivityForResult(Intent.createChooser(intent, "Open CSV"), 100)
                // importCSV()
            } else {
                requestReadWritePermission()
            }
        }

        binding?.switchOwp?.setOnCheckedChangeListener { compoundButton, b ->
            prefs.setOvEnabled(b)
        }

        binding?.switchPms?.setOnCheckedChangeListener { compoundButton, b ->
            prefs.setPmsEnabled(b)
        }

        binding?.secondaryPin?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding?.btnSave?.isVisible = text.toString().trim() != ""
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        binding?.btnSave?.setOnClickListener {
            if (!binding?.secondaryPin?.text.isNullOrEmpty() && binding?.secondaryPin?.text?.length == 6) {
                prefs.setSecondPassCode(true)
                binding?.secondaryPin?.text?.toString()?.let { it1 -> prefs.setSecondPassPin(it1) }
                binding?.secondaryPin?.text?.clear()
                Toast.makeText(context, "Secondary Password Successfully", Toast.LENGTH_SHORT).show()
            } else {
                binding?.secondaryPin?.text?.clear()
                Toast.makeText(context, "Enter 6 digit pin to continue", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if ((permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) && (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true)) {
            if (isExport) {
                exportCSV()
            } else {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "text/csv"
                startActivityForResult(Intent.createChooser(intent, "Open CSV"), 100)
            }
        } else {
            showRationalDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            data?.data?.let { importCSV(it) }
        }
    }

    private fun openAppSettings() {
        val packageUri =
            Uri.fromParts("package", requireContext().applicationContext.packageName, null)
        val applicationDetailsSettingsIntent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
        applicationDetailsSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        requireContext().applicationContext.startActivity(applicationDetailsSettingsIntent)
    }

    private fun showRationalDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.spermission)
            .setMessage(R.string.permission_details)
            .setCancelable(false)
            .setPositiveButton(
                R.string.open_settings
            ) { _: DialogInterface?, _: Int ->
                openAppSettings()
            }
            .setNegativeButton(
                R.string.cancel
            ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun requestReadWritePermission() {
        activityResultLauncher.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                    (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        } else false
    }

    private fun importCSV(uri: Uri) {

        val filePath = FileUtils.getPath(requireContext(), uri)
        // Log.i("TLogs", "importCSV: $newFilePath")

        if (File(filePath).exists()) {
            val csvReader = CSVReader(FileReader(filePath))
//            CSVReader(FileReader("${getExternalStorageDirectory()}/CSV/sym_table.csv")) path of local storage (it should be your csv file locatioin)
            var nextLine: Array<String>? = null
            var count = 0
            val columns = StringBuilder()
            GlobalScope.launch(IO) {
                do {
                    val value = StringBuilder()
                    nextLine = csvReader.readNext()
                    nextLine?.let { nextLine ->
                        for (i in 0 until nextLine.size - 1) {
                            if (count == 0) {                             // the count==0 part only read
                                if (i == nextLine.size - 2) {             //your csv file column name
                                    columns.append(nextLine[i])
                                    count = 1
                                } else
                                    columns.append(nextLine[i]).append(",")
                            } else {                         // this part is for reading value of each row
                                if (i == nextLine.size - 2) {
                                    value.append("'").append(nextLine[i]).append("'")
                                    count = 2
                                } else
                                    value.append("'").append(nextLine[i]).append("',")
                            }
                        }
                        if (count == 2) {
                            myViewModel.pushCustomerData(columns, value)//write here your code to insert all values
                        }
                    }
                } while ((nextLine) != null)
            }

            Toast.makeText(requireContext(), "File is imported", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "File is corrupt or can't be read", Toast.LENGTH_SHORT).show()
        }

    }

    private fun exportCSV() {
        Log.i("Tlogs", "exportCSV: ")
//        val exportDir = File(getExternalStorageDirectory(), "/CSV")// your path where you want save your file
        val exportDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + File.separator, "CSV")// your path where you want save your file
//        val exportDir = File(context?.filesDir?.absolutePath + File.separator, "CSV")// your path where you want save your file
        //  val exportDir = File(context?.dataDir?.absolutePath + File.separator, "CSV")// your path where you want save your file
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }

        val file = File(exportDir, "symptom_data_${System.currentTimeMillis()}.csv")//$TABLE_NAME.csv is like user.csv or any name you want to save
        try {
            file.createNewFile()
            val csvWrite = CSVWriter(FileWriter(file))
            val curCSV = myViewModel.myDatabase.query("SELECT * FROM sym_table", null) // query for get all data of your database table
            csvWrite.writeNext(curCSV.columnNames)
            while (curCSV.moveToNext()) {
                //Which column you want to export
                val arrStr = arrayOfNulls<String>(curCSV.columnCount)
                for (i in 0 until curCSV.columnCount - 1) {
                    arrStr[i] = curCSV.getString(i)
                }
                csvWrite.writeNext(arrStr)
            }
            csvWrite.close()
            curCSV.close()

            zipAndDelete(file)

        } catch (sqlEx: Exception) {

        }

    }

    private fun zipAndDelete(file: File) {

        val dialogSetPassword = Dialog(requireContext())
        dialogSetPassword.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogSetPassword.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogSetPassword.setContentView(R.layout.dialog_set_password)
        dialogSetPassword.create()
        dialogSetPassword.setCancelable(true)
        val window: Window? = dialogSetPassword.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialogSetPassword.show()

        val btnOk: TextView = dialogSetPassword.findViewById(R.id.btnOk)
        val btnCancel: TextView = dialogSetPassword.findViewById(R.id.btnCancel)
        val editText: EditText = dialogSetPassword.findViewById(R.id.et_password)

        btnOk.setOnClickListener {
            val exportDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + File.separator, "CSV")// your path where you want save your file
            if (editText.text.toString().trim() != "" && editText.text.length >= 6) {
                CoroutineScope(IO).launch {
                    val zipParameters = ZipParameters()
                    zipParameters.isEncryptFiles = true
                    zipParameters.encryptionMethod = EncryptionMethod.ZIP_STANDARD
                    zipParameters.aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256

                    val filesToAdd: List<File> = listOf(file)

                    val zipFile = ZipFile("$exportDir/${System.currentTimeMillis()} symptom_data.zip", editText.text.toString().toCharArray())
                    zipFile.addFiles(filesToAdd, zipParameters)
                    withContext(Main) {
                        Toast.makeText(requireContext(), "File is saved to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                        if (file.exists()) {
                            file.delete()
                        }

                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please enter password and length should be 6", Toast.LENGTH_SHORT).show()
            }
            dialogSetPassword.dismiss()
        }

        btnCancel.setOnClickListener {
            dialogSetPassword.dismiss()
        }


    }

    private fun moveToNext(destination: String) {
        if (findNavController().currentDestination?.id == R.id.settingsFragment) {
            when (destination) {
                "Home" -> {
                    findNavController().navigate(R.id.action_settingsFragment_to_homeFragment)
                }
                "Symptoms" -> {
                    findNavController().navigate(R.id.action_settingsFragment_to_symptomsFragment)
                }
            }
        }
    }

    private fun fragmentBackPress() {
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (exit) {
                    requireActivity().finishAndRemoveTask()
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.press_again_to_exit),
                        Toast.LENGTH_SHORT
                    ).show()
                    exit = true
                    countDown = object : CountDownTimer(3000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                        }

                        override fun onFinish() {
                            exit = false
                        }
                    }
                    countDown.start()

                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::callback.isInitialized) {
            callback.isEnabled = false
            callback.remove()
        }
    }

}