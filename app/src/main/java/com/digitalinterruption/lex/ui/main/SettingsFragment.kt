package com.digitalinterruption.lex.ui.main

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
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
import android.os.ParcelFileDescriptor
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
import com.digitalinterruption.lex.models.SymptomModel
import com.digitalinterruption.lex.ui.FileUtils
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import kotlinx.android.synthetic.main.fragment_pin_code.*
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

    private fun checkPinCollision(duressPin: String): Boolean {
        if (
            !prefs.getPin().isNullOrEmpty()
            && prefs.getPin() === duressPin
        ){
            return true
        }
        return false
    }

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
                // TODO: this doesn't actually do anything
               // val intent = Intent(Intent.ACTION_GET_CONTENT)
                //intent.addCategory(Intent.CATEGORY_OPENABLE)
                //intent.type = "text/csv"
                val intent = Intent()
                    .setType("*/*")
                    .setAction(Intent.ACTION_GET_CONTENT)
                startActivityForResult(Intent.createChooser(intent, "Open CSV"), 100)

                //importCSV()
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

        binding?.primaryPin?.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding?.btnSave?.isVisible = s.toString().trim() != ""
            }

            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
        })
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

            if(!binding?.primaryPin?.text.isNullOrEmpty() &&
                binding?.primaryPin?.text?.length==6){
                    binding?.primaryPin?.text?.toString()?.let { pin -> prefs.setPin(pin)
                    }
                binding?.primaryPin?.text?.clear()
                Toast.makeText(context, "Primary Pin Successfully", Toast.LENGTH_LONG).show()

            } else {
                binding?.secondaryPin?.text?.clear()
                Toast.makeText(context, "Enter 6 digit pin to continue", Toast.LENGTH_SHORT)
                    .show()
            }

            if (
                !binding?.secondaryPin?.text.isNullOrEmpty() &&
                binding?.secondaryPin?.text?.length == 6 &&
                binding?.secondaryPin?.text.toString().let { it1 -> checkPinCollision(it1) }
            ) {
                prefs.setDuressPinEnabled(true)
                binding?.secondaryPin?.text?.toString()?.let { pin -> prefs.setDuressPin(pin) }
                binding?.secondaryPin?.text?.clear()
                Toast.makeText(context, "Secondary Pin Successfully", Toast.LENGTH_SHORT).show()
            }else {
                if (binding?.secondaryPin?.text.toString().let { it1 -> checkPinCollision(it1) }) {
                    Toast.makeText(context, "Duress pin and Primary pin cannot be the same!", Toast.LENGTH_SHORT)
                        .show()
                }else {
                    Toast.makeText(context, "Enter 6 digit pin to continue", Toast.LENGTH_SHORT)
                    .show()
                }
                binding?.secondaryPin?.text?.clear()
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
                intent.type = "*/*"
                startActivityForResult(Intent.createChooser(intent, "Open CSV"), 100)
            }
        } else {
            showRationalDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            data?.getData()?.let {
                importCSV(requireContext(), it)
            }
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

    private fun importCSV(context: Context, uri: Uri) {

        val _csv = File.createTempFile("temp", "", context.cacheDir)
        _csv.outputStream().use{
            context.contentResolver.openInputStream(uri)?.copyTo(it)
        }

        if (_csv.exists()) {

            val csvReader = CSVReader(FileReader(_csv))
            var nextLine: Array<String>? = csvReader.readNext()
            var lineCount = 0
            val columns = StringBuilder()


            GlobalScope.launch(IO) {
                while ((nextLine) != null){
                    if (lineCount == 0){
                        lineCount += 1
                    }else{

                        nextLine?.let { nextLine ->
                            var _symptoms = SymptomModel(
                                    nextLine[0].toInt(),
                                    nextLine[1],
                                    nextLine[2],
                                    nextLine[3]
                                )
                            myViewModel.addData(arrayListOf(_symptoms))
                        }
                        lineCount += 1
                    }
                   nextLine = csvReader.readNext()

                }
            }

            Toast.makeText(requireContext(), "File is imported", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "File is corrupt or can't be read", Toast.LENGTH_SHORT).show()
        }

    }

    private fun exportCSV() {
        // TODO: Request export path

        val exportDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + File.separator, "CSV")// your path where you want save your file

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
                for (i in 0 until curCSV.columnCount) {
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
            // TODO: Select where to save export
            val exportDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + File.separator, "CSV")// your path where you want save your file
            if (
                editText.text.toString().trim() != "" &&
                editText.text.length >= 8) {
                CoroutineScope(IO).launch {
                    val zipParameters = ZipParameters()
                    zipParameters.isEncryptFiles = true
                    zipParameters.encryptionMethod = EncryptionMethod.ZIP_STANDARD
                    zipParameters.aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256

                    val filesToAdd: List<File> = listOf(file)

                    // TODO: change this it's shit
                    val zipFile = ZipFile("$exportDir/${System.currentTimeMillis()}_symptom_data.zip", editText.text.toString().toCharArray())
                    zipFile.addFiles(filesToAdd, zipParameters)
                    withContext(Main) {
                        Toast.makeText(requireContext(), "File is saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
                        if (file.exists()) {
                            file.delete()
                        }

                    }
                }
            } else {
                Toast.makeText(requireContext(), "Minimum Password Length is 8 characters", Toast.LENGTH_SHORT).show()
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