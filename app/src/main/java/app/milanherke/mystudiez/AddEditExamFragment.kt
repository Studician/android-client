package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_add_edit_exam.*
import java.util.*

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_EXAM = "exam"
private const val ARG_SUBJECT = "subject"

/**
 * A simple [Fragment] subclass.
 * This fragment was created to add or edit exams.
 * Activities that contain this fragment must implement the
 * [AddEditExamFragment.AddEditExamInteractions] interface
 * to handle interaction events.
 * Use the [AddEditExamFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEditExamFragment : Fragment() {

    private var exam: Exam? = null
    private var subject: Subject? = null
    private var listener: AddEditExamInteractions? = null
    private var listOfSubjects: ArrayList<Subject>? = null
    private var subjectIdClickedFromList: Long? = null
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(AddEditExamViewModel::class.java)
    }
    private val sharedViewModel by lazy {
        ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface AddEditExamInteractions {
        fun onSaveExamClicked(exam: Exam)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AddEditExamInteractions) {
            listener = context
        } else {
            throw RuntimeException("$context must implement AddEditExamInteractions")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exam = arguments?.getParcelable(ARG_EXAM)
        subject = arguments?.getParcelable(ARG_SUBJECT)
        listOfSubjects = sharedViewModel.getAllSubjects()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_edit_exam, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Avoiding problems with smart cast
        val exam = exam
        val subject = subject
        val listOfSubjects = listOfSubjects

        if (exam == null && subject == null) {
            // New exam is created. Fragment was called from ExamsFragment
            activity!!.toolbar.setTitle(R.string.add_new_exam_title)

            if (listOfSubjects != null) {
                if (listOfSubjects.size == 0) {
                    new_exam_subject_btn.text = getString(R.string.no_subjects_to_select_from)
                    new_exam_subject_btn.background =
                        resources.getDrawable(R.drawable.circular_disabled_button, null)
                    new_exam_subject_btn.setTextColor(resources.getColor(R.color.colorTextSecondary, null))
                    new_exam_subject_btn.isEnabled = false
                }
            }

        } else if (exam != null && subject != null) {
            // Exam is edited. Fragment was called from ExamDetailsFragment
            activity!!.toolbar.title =
                resources.getString(R.string.edit_subject_title, subject.name)
            new_exam_name.setText(exam.name)
            new_exam_desc.setText(exam.description)
            new_exam_subject_btn.text = subject.name
            new_exam_subject_btn.background =
                resources.getDrawable(R.drawable.circular_disabled_button, null)
            new_exam_subject_btn.setTextColor(resources.getColor(R.color.colorTextSecondary, null))
            new_exam_subject_btn.isEnabled = false
            new_exam_date_btn.text = exam.date
            new_exam_reminder_btn.text = exam.reminder

        } else if (exam == null && subject != null) {
            // New exam is created. Fragment was called from SubjectDetailsFragment
            activity!!.toolbar.setTitle(R.string.add_new_exam_title)
            new_exam_subject_btn.text = subject.name
            new_exam_subject_btn.background =
                resources.getDrawable(R.drawable.circular_disabled_button, null)
            new_exam_subject_btn.setTextColor(resources.getColor(R.color.colorTextSecondary, null))
            new_exam_subject_btn.isEnabled = false
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        activity!!.bar.visibility = View.GONE
        activity!!.fab.visibility = View.GONE

        if (new_exam_subject_btn.isEnabled) {
            new_exam_subject_btn.setOnClickListener {
                showSubjectsPopUp(it)
            }
        }

        new_exam_date_btn.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                context!!,
                CalendarUtils.getDate(activity!!, R.id.new_exam_date_btn, cal),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        new_exam_reminder_btn.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                context,
                CalendarUtils.getTime(activity!!, R.id.new_exam_reminder_btn, cal),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
            DatePickerDialog(
                context!!,
                CalendarUtils.getDate(activity!!, R.id.new_exam_reminder_btn, cal),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        new_exam_save_btn.setOnClickListener {
            saveExam()
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        FragmentsStack.getInstance(context!!).pop()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param exam The exam to be edited, or null when creating a new one.
         * @param subject Subject associated with the exam. Null if fragment was called from [ExamsFragment]
         * @return A new instance of fragment AddEditExamFragment.
         */
        @JvmStatic
        fun newInstance(exam: Exam? = null, subject: Subject? = null) =
            AddEditExamFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_EXAM, exam)
                    putParcelable(ARG_SUBJECT, subject)
                }
            }
    }

    /**
     * Creates a newExam object with the details to be saved, then
     * call the viewModel's saveExam function to save it
     * Exam is not a data class, so we can compare the new details with the original exam
     * and only save if they are different
     */
    private fun saveExam() {
        if (requiredFieldsAreFilled()) {
            val newExam = examFromUi()
            if (newExam != exam) {
                exam = viewModel.saveExam(newExam)
                listener?.onSaveExamClicked(exam!!)
            } else {
                Toast.makeText(
                    context!!,
                    getString(R.string.did_not_change),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                context!!,
                getString(R.string.required_fields_are_not_filled),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun examFromUi(): Exam {
        val exam = Exam(
            new_exam_name.text.toString(),
            new_exam_desc.text.toString(),
            subjectIdClickedFromList ?: (subject?.subjectId ?: -1L),
            new_exam_date_btn.text.toString(),
            if (new_exam_reminder_btn.text.toString() != getString(R.string.add_edit_lesson_btn)) new_exam_reminder_btn.text.toString() else ""
        )
        exam.examId = this.exam?.examId ?: 0
        return exam
    }

    private fun requiredFieldsAreFilled(): Boolean {
        if (new_exam_name.text.isNotEmpty()
            && new_exam_subject_btn.text.isNotEmpty()
            && new_exam_date_btn.text != getString(R.string.add_edit_lesson_btn)
            && new_exam_date_btn.text.isNotEmpty()
        ) {
            return true
        }
        return false
    }

    private fun showSubjectsPopUp(view: View) {
        // Avoiding problems with smart cast
        val listOfSubjects = listOfSubjects
        if (listOfSubjects != null) {
            val popupMenu = PopupMenu(activity!!, view)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.empty_menu, popupMenu.menu)

            // Adding the subjects to the list if it is not null
            for (subject in listOfSubjects) {
                popupMenu.menu.add(subject.name).setOnMenuItemClickListener {
                    new_exam_subject_btn.text = subject.name
                    subjectIdClickedFromList = subject.subjectId
                    true
                }
            }
            popupMenu.show()
        }
    }
}
