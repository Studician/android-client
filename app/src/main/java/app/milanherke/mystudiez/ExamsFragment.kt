package app.milanherke.mystudiez

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_exams.*

/**
 * A simple [Fragment] subclass.
 * Use the [ExamsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExamsFragment : Fragment(), ExamsRecyclerViewAdapter.OnExamClickListener {

    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(ExamsViewModel::class.java)
    }
    private val sharedViewModel by lazy {
        ViewModelProviders.of(activity!!).get(SharedViewModel::class.java)
    }
    private val examsAdapter = ExamsRecyclerViewAdapter(null, null, this)
    private var listener: ExamsInteractions? = null

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
    interface ExamsInteractions {
        fun examsFragmentIsBeingCreated()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ExamsInteractions) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ExamsInteractions")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadExams()
        viewModel.cursorExams.observe(
            this,
            Observer { cursor ->
                examsAdapter.swapExamsCursor(cursor)?.close()
                if (exam_list != null && cursor.count != 0) Animations.runLayoutAnimation(exam_list)
            }
        )
        // Listener will never be null since the program crashes in onAttach if the interface is not implemented
        listener!!.examsFragmentIsBeingCreated()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exams, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity!!.toolbar.setTitle(R.string.exams_title)

        exam_list.layoutManager = LinearLayoutManager(context)
        exam_list.adapter = examsAdapter
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }

        activity!!.bar.visibility = View.VISIBLE
        activity!!.fab.visibility = View.VISIBLE
    }

    override fun onDetach() {
        super.onDetach()
        FragmentsStack.getInstance(context!!).push(Fragments.EXAMS)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment ExamsFragment.
         */
        @JvmStatic
        fun newInstance() =
            ExamsFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    override fun onExamClickListener(exam: Exam) {
        activity!!.replaceFragmentWithTransition(
            ExamDetailsFragment.newInstance(exam),
            R.id.fragment_container
        )
    }

    override fun loadSubjectFromExam(id: Long): Subject? {
        return sharedViewModel.subjectFromId(id)
    }
}
