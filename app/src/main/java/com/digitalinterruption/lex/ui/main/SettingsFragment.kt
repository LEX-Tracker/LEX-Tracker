package com.digitalinterruption.lex.ui.main

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
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
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.digitalinterruption.lex.R
import com.digitalinterruption.lex.SharedPrefs
import com.digitalinterruption.lex.databinding.FragmentSettingsBinding
import com.digitalinterruption.lex.models.MyViewModel
import com.digitalinterruption.lex.models.SymptomModel
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding
    private lateinit var callback: OnBackPressedCallback
    var exit: Boolean = false
    lateinit var countDown: CountDownTimer
    val myViewModel: MyViewModel by viewModels()
    var isExport: Boolean = false
    lateinit var prefs: SharedPrefs
    private val defaultDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

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
        if (!checkPermission()){
            requestReadWritePermission()
        }

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
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                startActivityForResult(Intent.createChooser(intent, "Select Export Directory"), 200)
            } else {
                requestReadWritePermission()
            }
        }

        binding?.importLayout?.setOnClickListener {
            isExport = false
            if (checkPermission()) {
                val intent = Intent()
                    .setType("*/*")
                    .setAction(Intent.ACTION_GET_CONTENT)
                startActivityForResult(Intent.createChooser(intent, "Open CSV"), 100)
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
            if (
                !binding?.secondaryPin?.text.isNullOrEmpty() &&
                binding?.secondaryPin?.text?.length == 6
            ) {
                if (!binding?.secondaryPin?.text.toString().let {it1 -> checkPinCollision(it1)}){
                    prefs.setDuressPinEnabled(true)
                    binding?.secondaryPin?.text?.toString()?.let { pin -> prefs.setDuressPin(pin) }
                    binding?.secondaryPin?.text?.clear()
                    Toast.makeText(context, "Secondary Pin Successfully", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context, "Duress pin and Primary pin cannot be the same!", Toast.LENGTH_SHORT)
                        .show()
                }

            }else {
                    Toast.makeText(context, "Enter 6 digit pin to continue", Toast.LENGTH_SHORT)
                    .show()
                }
                binding?.secondaryPin?.text?.clear()
        }

    }


    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if ((permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) && (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true)) {


            if (isExport) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                startActivityForResult(Intent.createChooser(intent, "Select export directory"), 200)
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

        } else if(requestCode == 200){

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
            if (
                editText.text.toString().trim() != "" &&
                editText.text.length >= 8) {
                val _password = editText.text.toString()
                dialogSetPassword.dismiss()
                val dirUri = data?.data
                exportCSV(requireContext(), dirUri,_password)
            } else {
                    Toast.makeText(requireContext(), "Minimum Password Length is 8 characters", Toast.LENGTH_SHORT).show()
                    dialogSetPassword.dismiss()
                }
            }

            btnCancel.setOnClickListener {
                dialogSetPassword.dismiss()
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

            GlobalScope.launch(IO) {
                while ((nextLine) != null){
                    if (lineCount == 0){
                        lineCount += 1
                    }else{

                        nextLine?.let { nextLine ->
                            var _symptoms = SymptomModel(
                                    nextLine[0].toInt(), // id
                                    LocalDateTime.parse(nextLine[1]).format(defaultDateFormat),         // date
                                    sanitiseText(nextLine[2]),         // symptom
                                    nextLine[3]          // intensity
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

    private fun sanitiseText (inputText: String): String{
        // crude method of stripping dangerous characters from symptoms
        var text = inputText
        text = text.replace("[^\\x00-\\x7f]".toRegex(), "")
        text = text.replace("[\\p{Cntrl}&&[^\r\n\t]]".toRegex(), "")
        text = text.replace("\\p{C}".toRegex(), "")
        text = text.replace("=","")
        text = text.replace("+","")
        text = text.replace("-","")
        text = text.replace("@","")
        return text
    }
    private fun exportCSV(context: Context, uri: Uri?, password: String) {
        try {
            val dest = uri?.let { DocumentFile.fromTreeUri(context, it) }
            var _file: DocumentFile? = null
            var expDir: DocumentFile? = null
            val timeStamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm")
            ).toString()

            if (dest?.findFile("Exported")==null){
                dest?.createDirectory("Exported")
            }

            expDir = dest?.findFile("Exported")

            val _csv = File(context.cacheDir,"Symptoms_${timeStamp}.csv")

            _csv.outputStream().use{
                if (expDir?.uri != null) {

                    _file = expDir.createFile("application/zip", "symptoms_${
                        timeStamp
                    }.zip")

                    if (_file != null) {
                        context.contentResolver.openInputStream(_file!!.uri)?.copyTo(it)
                    }
                }
            }

            val csvWrite = CSVWriter(FileWriter(_csv))
            val curCSV = myViewModel.myDatabase.query("SELECT * FROM sym_table", null) // query for get all data of your database table
            csvWrite.writeNext(curCSV.columnNames)
            while (curCSV.moveToNext()) {

                val arrStr = arrayOfNulls<String>(curCSV.columnCount)
                for (i in 0 until curCSV.columnCount) {
                    arrStr[i] = curCSV.getString(i)
                }
                csvWrite.writeNext(arrStr)
            }
            csvWrite.close()
            curCSV.close()


            dest?.let { zipAndDelete(_csv.absolutePath, _file, password) }

        } catch (sqlEx: Exception) {
            Log.d("ERROR", sqlEx.toString())
        }

    }

    private fun zipAndDelete(filePath: String, dest: DocumentFile?, password: String) {
        var file: File

        val exportPath = dest?.uri?.path

        CoroutineScope(IO).launch {
            val zipParameters = ZipParameters()
            zipParameters.isEncryptFiles = true
            zipParameters.encryptionMethod = EncryptionMethod.ZIP_STANDARD
            zipParameters.aesKeyStrength = AesKeyStrength.KEY_STRENGTH_256

            file = File(filePath)
            val filesToAdd: List<File> = listOf(file)

            //Create temp zip file using File
            val tmpOut = File(context?.cacheDir, "temp.zip")


            val zipFile = ZipFile(tmpOut.canonicalPath, password.toCharArray())
            zipFile.addFiles(filesToAdd, zipParameters)
            zipFile.close()

            copyToDocumentFile(tmpOut, dest)

            withContext(Main) {
                Toast.makeText(
                    requireContext(),
                    "File is saved to $exportPath",
                    Toast.LENGTH_LONG
                ).show()
                if (file.exists()) {
                    file.delete()
                }

            }
        }
    }

    private fun copyToDocumentFile(f : File?, df : DocumentFile?){
        val context = requireContext()

        if (df != null && f !=null) {

            val _f = DocumentFile.fromFile(f)
            val contResolver = context.contentResolver
            val outStream = contResolver.openOutputStream(df.uri)

            outStream.use{
                if (it != null) {
                    contResolver.openInputStream(_f.uri)?.copyTo(it)
                }
            }
            if (f.exists()){
              f.delete()
            }
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