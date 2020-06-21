package com.orangeman.probemanapp.activity.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.orangeman.probemanapp.R
import com.orangeman.probemanapp.customview.domain.Recognition
import com.orangeman.probemanapp.db.repository.ProfileRepository
import com.orangeman.probemanapp.db.repository.domain.Profile
import com.orangeman.probemanapp.util.DirectoryUtil
import com.orangeman.probemanapp.util.ImageUtil
import com.orangeman.probemanapp.util.domain.ConstValues
import java.io.File


open class ProfileListAdapter(
    private val context: Context,
    private val profileList: ArrayList<Profile>
) : BaseAdapter() {
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val profileRepository = ProfileRepository(context)

    private lateinit var profileImageView: ImageView

    private lateinit var salaryValueTextView: TextView
    private lateinit var salaryPercentageTextView: TextView
    private lateinit var marriageWillnessValueTextView: TextView
    private lateinit var marriageWillnessPercentageTextView: TextView
    private lateinit var familyInfoValueTextView: TextView
    private lateinit var familyInfoPercentageTextView: TextView
    private lateinit var graduationValueTextView: TextView
    private lateinit var graduationPercentageTextView: TextView
    private lateinit var drinkValueTextView: TextView
    private lateinit var drinkPercentageTextView: TextView
    private lateinit var smokeValueTextView: TextView
    private lateinit var smokePercentageTextView: TextView

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.profile_list_item, parent, false)

        profileImageView = rowView.findViewById(R.id.profile_image)
        val filePath = "${DirectoryUtil.appRootFolder(context)?.absolutePath}/${profileList[position].imageName}"
        val bitmap = ImageUtil.decodeFile(File(filePath))
        bitmap?.let {
            val drawable = BitmapDrawable(context.resources, it)
            profileImageView.setImageDrawable(drawable)
        }

        rowView.setOnLongClickListener { _ ->
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.dialog_confirm_title)
                .setMessage(R.string.dialog_delete_profile_confirm_content)
                .setPositiveButton(R.string.dialog_yes, DialogInterface.OnClickListener { _, _ ->
                    profileList.removeAt(position)
                    profileRepository.resave(profileList)
                    this.notifyDataSetChanged()
                })
                .setNegativeButton(R.string.dialog_no, DialogInterface.OnClickListener { _, _ -> })
                .show()
            true
        }

        // salary
        salaryValueTextView = rowView.findViewById(R.id.salary_value_textview)
        salaryPercentageTextView = rowView.findViewById(R.id.salary_percentage_textview)

        // marriageWillness
        marriageWillnessValueTextView = rowView.findViewById(R.id.marriage_willness_value_textview)
        marriageWillnessPercentageTextView = rowView.findViewById(R.id.marriage_willness_percentage_textview)

        // familyInfo
        familyInfoValueTextView = rowView.findViewById(R.id.family_info_value_textview)
        familyInfoPercentageTextView = rowView.findViewById(R.id.family_info_percentage_textview)

        // graduation
        graduationValueTextView = rowView.findViewById(R.id.graduation_value_textview)
        graduationPercentageTextView = rowView.findViewById(R.id.graduation_percentage_textview)

        // drink
        drinkValueTextView = rowView.findViewById(R.id.drink_value_textview)
        drinkPercentageTextView = rowView.findViewById(R.id.drink_percentage_textview)

        // smoke
        smokeValueTextView = rowView.findViewById(R.id.smoke_value_textview)
        smokePercentageTextView = rowView.findViewById(R.id.smoke_percentage_textview)

        showResultsInBottomSheet(profileList[position].recognitions)
        return rowView
    }

    private fun showResultsInBottomSheet(recognitionMap: Map<Int, List<Recognition>>) {
        val salaryRecognition = recognitionMap[ConstValues.LabelIndex.Salary.value]?.get(0)
        if (salaryRecognition != null) {
            salaryValueTextView.text = salaryRecognition.title
            salaryPercentageTextView.text = String.format("%.1f%%", salaryRecognition.confidence * 100.0f)
        }

        val marriageWillnessRecognition = recognitionMap[ConstValues.LabelIndex.MarriageWillness.value]?.get(0)
        if (marriageWillnessRecognition != null) {
            marriageWillnessValueTextView.text = marriageWillnessRecognition.title
            marriageWillnessPercentageTextView.text = String.format("%.1f%%", marriageWillnessRecognition.confidence * 100.0f)
        }

        val familyInfoRecognition = recognitionMap[ConstValues.LabelIndex.FamilyInfo.value]?.get(0)
        if (familyInfoRecognition != null) {
            familyInfoValueTextView.text = familyInfoRecognition.title
            familyInfoPercentageTextView.text = String.format("%.1f%%", familyInfoRecognition.confidence * 100.0f)
        }

        val graduationRecognition = recognitionMap[ConstValues.LabelIndex.Graduation.value]?.get(0)
        if (graduationRecognition != null) {
            graduationValueTextView.text = graduationRecognition.title
            graduationPercentageTextView.text = String.format("%.1f%%", graduationRecognition.confidence * 100.0f)
        }

        val drinkRecognition = recognitionMap[ConstValues.LabelIndex.Drink.value]?.get(0)
        if (drinkRecognition != null) {
            drinkValueTextView.text = drinkRecognition.title
            drinkPercentageTextView.text = String.format("%.1f%%", drinkRecognition.confidence * 100.0f)
        }

        val smokeRecognition = recognitionMap[ConstValues.LabelIndex.Smoke.value]?.get(0)
        if (smokeRecognition != null) {
            smokeValueTextView.text = smokeRecognition.title
            smokePercentageTextView.text = String.format("%.1f%%", smokeRecognition.confidence * 100.0f)
        }
    }

    override fun getItem(position: Int): Any {
        return profileList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return profileList.size
    }

}
