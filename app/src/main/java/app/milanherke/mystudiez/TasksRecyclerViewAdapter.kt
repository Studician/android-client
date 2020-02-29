package app.milanherke.mystudiez

import android.database.Cursor
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.details_list_item.view.*

private const val VIEW_TYPE_NOT_EMPTY = 0
private const val VIEW_TYPE_EMPTY = 1
private const val VIEW_TYPE_ALL_TASKS_EMPTY = 2
private const val VIEW_TYPE_EMPTY_IN_OVERVIEW = 3

class TasksRecyclerViewAdapter(
    private var cursorTasks: Cursor?,
    private var subjectIndicator: Drawable?,
    private val listener: OnTaskClickListener,
    private val calledFromOverview: Boolean? = null
) :
    RecyclerView.Adapter<TasksRecyclerViewAdapter.ViewHolder>() {

    interface OnTaskClickListener {
        fun onTaskClickListener(task: Task)
        fun loadSubjectFromTask(id: Long): Subject?
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_EMPTY_IN_OVERVIEW -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.no_tasks_overview, parent, false)
                EmptyTaskViewHolder(view)
            }
            VIEW_TYPE_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.no_task_list_item, parent, false)
                EmptyTaskViewHolder(view)
            }
            VIEW_TYPE_ALL_TASKS_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.no_tasks_for_any_subject, parent, false)
                EmptyTaskViewHolder(view)
            }
            VIEW_TYPE_NOT_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.details_list_item, parent, false)
                TaskViewHolder(view)
            }
            else -> throw IllegalStateException("Couldn't recognise the view type")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cursor = cursorTasks
        when (getItemViewType(position)) {
            VIEW_TYPE_EMPTY, VIEW_TYPE_ALL_TASKS_EMPTY, VIEW_TYPE_EMPTY_IN_OVERVIEW -> {
                // We are not putting any data into the empty view, therefore we do not need to do anything here
            }
            VIEW_TYPE_NOT_EMPTY -> {
                if (cursor != null) {
                    if (!cursor.moveToPosition(position)) {
                        throw IllegalStateException("Couldn't move to position $position")
                    }

                    // Create Task from the data in the cursor
                    val task = Task(
                        cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_NAME)),
                        cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_DESCRIPTION)),
                        cursor.getInt(cursor.getColumnIndex(TasksContract.Columns.TASK_TYPE)),
                        cursor.getLong(cursor.getColumnIndex(TasksContract.Columns.TASK_SUBJECT)),
                        cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_DUEDATE)),
                        cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_REMINDER))
                    )

                    // Id is not set in the constructor
                    task.taskId = cursor.getLong(cursor.getColumnIndex(TasksContract.Columns.ID))

                    holder.bind(task)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        val cursor = cursorTasks
        return if (cursor == null || cursor.count == 0) 1 else cursor.count
    }

    override fun getItemViewType(position: Int): Int {
        val cursor = cursorTasks
        return if (subjectIndicator == null && (cursor == null || cursor.count == 0)) {
            if (calledFromOverview == true) {
                VIEW_TYPE_EMPTY_IN_OVERVIEW
            } else {
                VIEW_TYPE_ALL_TASKS_EMPTY
            }
        } else if (subjectIndicator != null && (cursor == null || cursor.count == 0)) {
            VIEW_TYPE_EMPTY
        } else {
            VIEW_TYPE_NOT_EMPTY
        }
    }

    /**
     * Swap in the drawable of the fragment details
     *
     * @param drawable The new drawable object to be used
     */
    fun swapDrawable(drawable: Drawable) {
        subjectIndicator = drawable
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.
     * The returned old Cursor is *not* closed.
     *
     * @param newCursor The new cursor to be used
     * @return Returns the previously set Cursor, or null if there wasn't one
     * If the given new Cursor is the same instance as the previously set Cursor, null is also returned
     */
    fun swapTasksCursor(newCursor: Cursor?): Cursor? {
        if (newCursor === cursorTasks) {
            return null
        }
        val numItems = itemCount
        val oldCursor = cursorTasks
        cursorTasks = newCursor
        if (newCursor != null) {
            //notify the observers about the new cursor
            notifyDataSetChanged()
        } else {
            //notify the observers about the lack of a data set
            notifyItemRangeRemoved(0, numItems)
        }
        return oldCursor
    }

    open class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {
        open fun bind(task: Task) {}
    }

    private inner class TaskViewHolder(override val containerView: View) :
        ViewHolder(containerView) {

        override fun bind(task: Task) {
            // Avoiding problems with smart-cast
            val calledFromOverview = calledFromOverview

            // If the subjectIndicator is not null, then the recycler view is being used in SubjectDetailsFragment
            // Meaning we do not have to load nor display the subject details
            if (subjectIndicator != null) {
                containerView.details_list_title.text = task.name
                containerView.details_list_header1.text = task.dueDate
                containerView.details_list_header2.text = TaskUtils.getTaskType(task.type, containerView.context)

                // We're creating a clone because we do not want to affect the other instances
                containerView.details_list_subject_indicator.setImageDrawable(subjectIndicator)
            } else {
                val subject = listener.loadSubjectFromTask(task.subjectId)
                // If the subject is null, it means that the task we're trying to load has been deleted
                if (subject != null) {
                    // After null-check, we must decide if the ViewHolder is being used in OverviewFragment
                    // And if so, we need to display the details in a different way
                    if (calledFromOverview != null) {
                        if (calledFromOverview == true) {
                            containerView.details_list_title.text = task.name
                            containerView.details_list_header1.text = subject.name
                            containerView.details_list_header2.visibility = View.GONE
                        } else {
                            throw IllegalStateException("TaskViewHolder is being used in unrecognised fragment")
                        }
                    } else {
                        containerView.details_list_title.text = task.name
                        containerView.details_list_header1.text = containerView.resources.getString(
                            R.string.details_subject_item_time,
                            subject.name,
                            TaskUtils.getTaskType(task.type, containerView.context)
                        )
                        containerView.details_list_header2.text = task.dueDate
                    }

                    //Creating a clone drawable because we do not want to affect other instances of the original drawable
                    val clone =
                        containerView.resources.getDrawable(R.drawable.placeholder_circle, null)
                            .mutatedClone()
                    clone.displayColor(subject.colorCode, containerView.context)
                    containerView.details_list_subject_indicator.setImageDrawable(clone)
                }
            }

            containerView.details_list_container.setOnClickListener {
                listener.onTaskClickListener(task)
            }
        }

    }

    // We do not need to override the bind method since we're not putting any data into the empty view
    private class EmptyTaskViewHolder(override val containerView: View) : ViewHolder(containerView)

}