package com.example.grocerly.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerly.R
import com.example.grocerly.adapter.RecentCartsAdapter
import com.example.grocerly.data.database.AppDatabase
import com.example.grocerly.ui.insights.AreaChartView
import com.example.grocerly.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class InsightsFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var recentAdapter: RecentCartsAdapter

    private val idLocale: Locale by lazy {
        Locale.Builder()
            .setLanguage("id")
            .setRegion("ID")
            .build()
    }

    private val currency: NumberFormat by lazy {
        NumberFormat.getCurrencyInstance(idLocale)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_insights, container, false)

        db = AppDatabase.getInstance(requireContext())

        val tvTotalSpending = view.findViewById<TextView>(R.id.tvTotalSpending)
        val chart = view.findViewById<AreaChartView>(R.id.areaChart)

        val rvRecent = view.findViewById<RecyclerView>(R.id.rvRecentCarts)
        rvRecent.layoutManager = LinearLayoutManager(requireContext())

        recentAdapter = RecentCartsAdapter()
        rvRecent.adapter = recentAdapter

        val fromMillis = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)

        viewLifecycleOwner.lifecycleScope.launch {
            val session = SessionManager(requireContext())
            val ownerEmail = if (session.isLogin()) session.getEmail() else "guest"

            db.purchaseDao()
                .getDailySpendingFlow(fromMillis, ownerEmail)
                .collect { days ->

                    val total = days.sumOf { it.total }
                    tvTotalSpending.text = currency.format(total)

                    val dayMap = days.associate { it.dayStart to it.total.toFloat() }

                    val todayStart =
                        (System.currentTimeMillis() / 86_400_000L) * 86_400_000L
                    val startDay = todayStart - (29L * 86_400_000L)

                    val points = ArrayList<Float>(30)
                    for (i in 0 until 30) {
                        val d = startDay + i * 86_400_000L
                        points.add(dayMap[d] ?: 0f)
                    }

                    chart.setValues(points)
                }
        }

        val session = SessionManager(requireContext())
        val ownerEmail = if (session.isLogin()) {
            session.getEmail()
        } else {
            getString(R.string.guest_user)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            db.purchaseDao()
                .getRecentFinishedCartsFlow(ownerEmail)
                .collect { carts ->
                    recentAdapter.submitList(carts)
                }
        }

        return view
    }
}
