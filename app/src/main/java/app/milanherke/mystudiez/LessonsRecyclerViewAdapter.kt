package app.milanherke.mystudiez

import android.database.Cursor
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.details_list_item.view.*
import java.lang.IllegalStateException

private const val VIEW_TYPE_NOT_EMPTY = 0
private const val VIEW_TYPE_EMPTY = 1
private const val VIEW_TYPE_PLACEHOLDER = 2

class LessonsRecyclerViewAdapter(private var cursorLessons: Cursor?) :
    RecyclerView.Adapter<LessonsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.no_lesson_list_item, parent, false)
                EmptyLessonViewHolder(view)
            }
            VIEW_TYPE_NOT_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.details_list_item, parent, false)
                LessonViewHolder(view)
            }
            VIEW_TYPE_PLACEHOLDER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_placeholder, parent, false)
                LessonViewHolder(view)
            }
            else -> throw IllegalStateException("Couldn't recognise the view type")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cursor = cursorLessons
        when (getItemViewType(position)) {
            VIEW_TYPE_EMPTY -> {
                // We are not putting any data into the empty view, therefore we do not need to do anything here
            }
            VIEW_TYPE_NOT_EMPTY -> {
                if (cursor != null) {
                    if (!cursor.moveToPosition(position)) {
                        throw IllegalStateException("Couldn't move cursor to position $position")
                    }

                    // Create Lesson from the data in the cursor
                    val lesson = Lesson(
                        cursor.getString(cursor.getColumnIndex(LessonsContract.Columns.LESSON_NAME)),
                        cursor.getString(cursor.getColumnIndex(LessonsContract.Columns.LESSON_WEEK)),
                        cursor.getString(cursor.getColumnIndex(LessonsContract.Columns.LESSON_DAY)),
                        cursor.getString(cursor.getColumnIndex(LessonsContract.Columns.LESSON_STARTS)),
                        cursor.getString(cursor.getColumnIndex(LessonsContract.Columns.LESSON_ENDS)),
                        cursor.getString(cursor.getColumnIndex(LessonsContract.Columns.LESSON_LOCATION))
                    )
                    // Id is not set in the instructor
                    lesson.id = cursor.getLong(cursor.getColumnIndex(LessonsContract.Columns.ID))

                    holder.bind(lesson)
                }
            }
            VIEW_TYPE_PLACEHOLDER -> {
                // We are not putting any data into the empty view, therefore we do not need to do anything here
            }
        }
    }

    override fun getItemCount(): Int {
        val cursor = cursorLessons
        return if (cursor == null || cursor.count == 0) 1 else cursor.count
    }

    override fun getItemViewType(position: Int): Int {
        val cursor = cursorLessons
        return if (cursor == null) VIEW_TYPE_PLACEHOLDER else if (cursor.count == 0) VIEW_TYPE_EMPTY else VIEW_TYPE_NOT_EMPTY
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.
     * The returned old Cursor is *not* closed.
     *
     * @param newCursor The new cursor to be used
     * @return Returns the previously set Cursor, or null if there wasn't one
     * If the given new Cursor is the same instance as the previously set Cursor, null is also returned
     */
    fun swapLessonsCursor(newCursor: Cursor?): Cursor? {
        if (newCursor === cursorLessons) {
            return null
        }
        val numItems = itemCount
        val oldCursor = cursorLessons
        cursorLessons = newCursor
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
        open fun bind(lesson: Lesson) {}
    }

    private class LessonViewHolder(override val containerView: View) : ViewHolder(containerView) {

        override fun bind(lesson: Lesson) {
            containerView.details_list_title.text = lesson.day
            containerView.details_list_header1.text = containerView.resources.getString(
                R.string.details_subject_item_time,
                lesson.starts,
                lesson.ends
            )
            containerView.details_list_header2.text = lesson.location
        }

    }

    // We do not need to override the bind method since we're not putting any data into the empty view
    private class EmptyLessonViewHolder(override val containerView: View) :
        ViewHolder(containerView)

}