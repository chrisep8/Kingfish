package com.directdev.portal.features.resources

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.directdev.portal.R
import com.directdev.portal.utils.getInitials
import com.directdev.portal.utils.snack
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.fragment_resources.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.alert
import org.jetbrains.anko.runOnUiThread
import javax.inject.Inject

class ResourcesFragment : Fragment(), AnkoLogger, ResourcesContract.View {
    override fun showFailed(message: String) {
        runOnUiThread {
            view?.snack(message, Snackbar.LENGTH_INDEFINITE)
        }
    }

    override fun showLoading() {
        runOnUiThread {
            resourceSyncProgress.visibility = View.VISIBLE
        }
    }

    override fun hideLoading() {
        runOnUiThread {
            resourceSyncProgress.visibility = View.GONE
        }
    }

    override fun showSuccess(message: String) {
        runOnUiThread {
            view?.snack(resourcesToolbar.title.toString() + " " + message, Snackbar.LENGTH_SHORT)
        }
    }

    override fun getSemester(): String = resourcesToolbar.title.toString()

    @Inject override lateinit var fbAnalytics: FirebaseAnalytics
    @Inject override lateinit var presenter: ResourcesContract.Presenter
    lateinit var adapter: ResourcesFragmentPagerAdapter
    lateinit var termList: List<Pair<Int, String>>
    var toolbarTitle = ""

    override fun onAttach(context: Context?) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            AndroidInjection.inject(this)
            adapter = ResourcesFragmentPagerAdapter(childFragmentManager)
            termList = presenter.getSemesters()
            presenter.updateSelectedSemester(termList.last().first)
        }
        super.onAttach(context)
    }

    override fun onAttach(activity: Activity?) {
        if (android.os.Build.VERSION.SDK_INT < 23) {
            AndroidInjection.inject(this)
            adapter = ResourcesFragmentPagerAdapter(childFragmentManager)
            termList = presenter.getSemesters()
            presenter.updateSelectedSemester(termList.last().first)
        }
        super.onAttach(activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_resources, container, false)
        val semesterFab = view.findViewById<FloatingActionButton>(R.id.semesterFab)
        val toolbar = view.findViewById<Toolbar>(R.id.resourcesToolbar)
        val tab = view.findViewById<TabLayout>(R.id.tabs)
        val viewPager = view.findViewById<ViewPager>(R.id.tabViewPager)
        viewPager.adapter = adapter
        tab.setupWithViewPager(viewPager)
        if (toolbarTitle == "") toolbarTitle = termList.last().second
        toolbar.title = toolbarTitle
        semesterFab.setOnClickListener {
            alert {
                items(termList.map { it.second }) { _, o ->
                    toolbarTitle = termList[o].second
                    toolbar.title = termList[o].second
                    presenter.updateSelectedSemester(termList[o].first)
                }
            }.show()
        }
        return view
    }

    override fun updateCourses(courses: List<Triple<String, String, Int>>) {
        adapter.clear()
        courses.map {
            adapter.addFrag(ResourcesListFragment.newInstance(
                    "${it.first} (${it.second})", it.third),
                    it.first.getInitials()
            )
        }
        adapter.notifyDataSetChanged()
    }

    override fun onStart() {
        super.onStart()
        val bundle = Bundle()
        bundle.putString("content", "resources")
        fbAnalytics.logEvent("content_opened", bundle)
    }

    override fun onStop() {
        presenter.onStop()
        super.onStop()
    }
}