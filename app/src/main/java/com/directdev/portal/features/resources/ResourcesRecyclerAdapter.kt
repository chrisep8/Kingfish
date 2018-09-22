package com.directdev.portal.features.resources

import android.app.DownloadManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Debug
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.crashlytics.android.Crashlytics
import com.directdev.portal.R
import com.directdev.portal.models.ResModel
import com.directdev.portal.models.ResResourcesModel
import kotlinx.android.synthetic.main.item_resources.view.*
import org.jetbrains.anko.downloadManager
import org.jetbrains.anko.layoutInflater

class ResourcesRecyclerAdapter(
        val context: Context,
        val data: List<String>,
        val resources: ResModel) :
        RecyclerView.Adapter<ResourcesRecyclerAdapter.ViewHolder>() {
    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(context.layoutInflater.inflate(R.layout.item_resources, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val outlines = resources.resources.filter { it.courseOutlineTopicID == data[position] }
        holder.bindData(context, outlines, resources)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindData(ctx: Context, item: List<ResResourcesModel>, resources: ResModel) {
            if (item.isEmpty()) return
            itemView.resSession.text = "Meeting " + item[0].sessionIDNUM
            itemView.resTopic.text = item[0].courseOutlineTopic
            try {
                itemView.presentationDownload.backgroundTintList = ColorStateList
                        .valueOf(Color.parseColor(ctx.getString(R.color.colorAccent)))
            } catch (e: NoSuchMethodError) {
            }
            itemView.presentationDownload.setOnClickListener {
                val selectedDownload = resources.path.filter {
                    it.mediaTypeId == "01" && it.courseOutlineTopicID == item[0].courseOutlineTopicID
                }
                if (selectedDownload.isEmpty()) return@setOnClickListener
                try {
                    val path = (selectedDownload[0].path
                            + selectedDownload[0].location
                            + "/"
                            + selectedDownload[0].filename)
                            .replace("\\", "/")
                            .replace(" ", "%20")
                    Log.d("Path", path)
                    ctx.downloadManager.enqueue(DownloadManager.Request(Uri.parse(path))
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setTitle(item[0].courseOutlineTopic))
                } catch (e: IllegalArgumentException) {
                    // Needed to know what changed when downloading slides
                    Crashlytics.log(selectedDownload.toString())
                    val path = "https://newcontent.binus.ac.id/data_content/" + (selectedDownload[0].path
                            + "/"
                            + selectedDownload[0].location
                            + "/"
                            + selectedDownload[0].filename)
                            .replace("\\", "/")
                            .replace(" ", "%20")
                    ctx.downloadManager.enqueue(DownloadManager.Request(Uri.parse(path))
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setTitle(item[0].courseOutlineTopic))
                }
            }
        }
    }
}