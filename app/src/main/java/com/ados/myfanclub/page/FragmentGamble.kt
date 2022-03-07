package com.ados.myfanclub.page

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import com.ados.myfanclub.MainActivity
import com.ados.myfanclub.R
import com.ados.myfanclub.databinding.FragmentGambleBinding
import com.ados.myfanclub.dialog.GemQuestionDialog
import com.ados.myfanclub.model.GemQuestionDTO
import com.ados.myfanclub.model.LogDTO
import com.ados.myfanclub.viewmodel.FirebaseViewModel
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.gem_question_dialog.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentGamble.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentGamble : Fragment() {
    enum class GambleType {
        GAMBLE_10, GAMBLE_30, GAMBLE_100
    }

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentGambleBinding? = null
    private val binding get() = _binding!!

    private val firebaseViewModel : FirebaseViewModel by viewModels()

    private lateinit var callback: OnBackPressedCallback
    private var toast : Toast? = null

    private var mIsBusy = false
    private var mGambleType = GambleType.GAMBLE_10
    private var mGambleCount = 0L
    private var mGambleCompleteCount = 0L
    private var currentDate = "" // 12시 지나서 날짜 변경을 체크하기 위한 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentGambleBinding.inflate(inflater, container, false)
        var rootView = binding.root.rootView

        //currentDate  = SimpleDateFormat("yyyyMMdd").format(Date())

        binding.layoutResult.visibility = View.GONE
        binding.buttonGambleResult.visibility = View.GONE
        binding.buttonGambleFinish.visibility = View.GONE
        binding.imgDiamond.visibility = View.GONE
        binding.textGambleCount.visibility = View.GONE
        binding.layoutPremium.visibility = View.GONE

        showGambleCount {
            binding.textGambleCount.visibility = View.VISIBLE
        }

        return rootView
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            callBackPressed()
        }

        binding.buttonGamble10.setOnClickListener {
            mGambleType = GambleType.GAMBLE_10
            gambleStart()
        }

        binding.buttonGamble30.setOnClickListener {
            mGambleType = GambleType.GAMBLE_30
            gambleStart()
        }

        binding.buttonGamble100.setOnClickListener {
            mGambleType = GambleType.GAMBLE_100
            gambleStart()
        }

        binding.buttonGambleResult.setOnClickListener {
            (binding.imgDiamond.background as AnimationDrawable).stop()
            binding.buttonGambleResult.visibility = View.GONE

            gambleResult()
        }

        binding.buttonGambleFinish.setOnClickListener {
            Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.gamble_back).fitCenter().into(binding.imgBackground)
            binding.imgDiamond.visibility = View.GONE
            binding.buttonGambleResult.visibility = View.GONE
            binding.buttonGambleFinish.visibility = View.GONE
            binding.layoutResult.visibility = View.GONE
            binding.layoutButton.visibility = View.VISIBLE
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                callBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    private fun callBackPressed() {
        if (mIsBusy) {
            callToast("뽑기 중에는 종료할 수 없습니다.")
        } else {
            finishFragment()
        }
    }

    private fun finishFragment() {
        val fragment = FragmentAccountInfo()
        parentFragmentManager.beginTransaction().apply{
            replace(R.id.layout_fragment, fragment)
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            addToBackStack(null)
            commit()
        }
    }

    private fun showGambleCount(myCallback: (Boolean) -> Unit) {
        checkGambleCount {
            if (it) {
                var user = (activity as MainActivity?)?.getUser()!!
                var gambleCount = (activity as MainActivity?)?.getPreferences()?.usedGambleCount!!
                if (user.isPremium()) {  // 프리미엄 패키지 사용중이라면 뽑기 횟수 두배
                    gambleCount = gambleCount.times(2)
                    binding.layoutPremium.visibility = View.VISIBLE
                } else {
                    binding.layoutPremium.visibility = View.GONE
                }
                mGambleCount = gambleCount.minus(mGambleCompleteCount)

                binding.textGambleCount.text = "오늘 남은 뽑기 횟수 : $mGambleCount"
            }
            myCallback(it)
        }
    }

    private fun checkGambleCount(myCallback: (Boolean) -> Unit) {
        val checkDate = SimpleDateFormat("yyyyMMdd").format(Date())
        if (currentDate != checkDate) { // 날짜가 바뀌었다면 남은 횟수 다시 체크
            currentDate = checkDate
            var user = (activity as MainActivity?)?.getUser()!!
            firebaseViewModel.getTodayCompleteGambleCount(user.uid.toString()) {
                mGambleCompleteCount = it
                myCallback(true)
            }
        } else {
            myCallback(true)
        }
    }

    private fun gambleStart() {
        showGambleCount {
            if (mGambleCount <= 0) {
                callToast("오늘은 더이상 뽑기를 할 수 없습니다. 내일 다시 이용해 주세요.")
                //날짜 바뀌었을 때 처리 가능하도록 갱신 가능한 코드 구현 필요
            } else {
                val preferencesDTO = (activity as MainActivity?)?.getPreferences()!!
                var price = 0
                var maxDiamond = 0
                when (mGambleType) {
                    GambleType.GAMBLE_10 -> {
                        price = preferencesDTO.priceGamble10!!
                        maxDiamond = 10
                    }
                    GambleType.GAMBLE_30 -> {
                        price = preferencesDTO.priceGamble30!!
                        maxDiamond = 30
                    }
                    GambleType.GAMBLE_100 -> {
                        price = preferencesDTO.priceGamble100!!
                        maxDiamond = 100
                    }
                }

                val question = GemQuestionDTO("다이아를 소모하여 1~${maxDiamond} 다이아 뽑기를 하시겠습니까?", price)
                val questionDialog = GemQuestionDialog(requireContext(), question)
                questionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                questionDialog.setCanceledOnTouchOutside(false)
                questionDialog.show()
                questionDialog.button_gem_question_cancel.setOnClickListener { // No
                    questionDialog.dismiss()
                }
                questionDialog.button_gem_question_ok.setOnClickListener { // Ok
                    questionDialog.dismiss()

                    var user = (activity as MainActivity?)?.getUser()!!
                    if ((user.paidGem!! + user.freeGem!!) < price!!) {
                        callToast("다이아가 부족합니다.")
                    } else {
                        /*(activity as MainActivity?)?.loading()
                        // 다이아 차감
                        val oldPaidGemCount = user.paidGem!!
                        val oldFreeGemCount = user.freeGem!!
                        firebaseViewModel.useUserGem(user.uid.toString(), price!!) {
                            var log = LogDTO("[다이아 차감] ${maxDiamond}다이아 뽑기로 ${price} 다이아 사용 (paidGem : $oldPaidGemCount -> ${user?.paidGem}, freeGem : $oldFreeGemCount -> ${user?.freeGem})", Date())
                            firebaseViewModel.writeUserLog(user?.uid.toString(), log) { }

                            (activity as MainActivity?)?.loadingEnd()
                            callToast("뽑기에 다이아가 소모되었습니다.")

                            binding.buttonGamble10.visibility = View.GONE
                            binding.buttonGamble30.visibility = View.GONE
                            binding.buttonGamble100.visibility = View.GONE
                            binding.buttonGambleResult.visibility = View.VISIBLE
                            binding.imgDiamond.visibility = View.VISIBLE

                            (binding.imgDiamond.background as AnimationDrawable).start()
                        }*/

                        mIsBusy = true
                        binding.layoutButton.visibility = View.GONE
                        binding.buttonGambleResult.visibility = View.VISIBLE
                        binding.imgDiamond.visibility = View.VISIBLE

                        (binding.imgDiamond.background as AnimationDrawable).start()
                    }
                }
            }
        }
    }

    private fun gambleResult() {
        var setPercent = arrayListOf<Int>()
        val preferencesDTO = (activity as MainActivity?)?.getPreferences()!!
        var price = 0
        var maxDiamond = 0
        when (mGambleType) { // 뽑기 확률 설정 (총 합이 10000이어야 함)
            GambleType.GAMBLE_10 -> {
                price = preferencesDTO.priceGamble10!!
                maxDiamond = 10
                setPercent.add(80)      // 1, 0.8%
                setPercent.add(300)     // 2, 3%
                setPercent.add(500)     // 3, 5%
                setPercent.add(1000)    // 4, 10%
                setPercent.add(3000)    // 5, 30%
                setPercent.add(3000)    // 6, 30%
                setPercent.add(1500)    // 7, 15%
                setPercent.add(500)     // 8, 5%
                setPercent.add(100)     // 9, 1%
                setPercent.add(20)      // 10, 0.2%
            }
            GambleType.GAMBLE_30 -> {
                price = preferencesDTO.priceGamble30!!
                maxDiamond = 30
                setPercent.add(50)      // 1, 0.5%
                setPercent.add(50)      // 2, 0.5%
                setPercent.add(100)     // 3, 1%
                setPercent.add(100)     // 4, 1%
                setPercent.add(200)     // 5, 2%
                setPercent.add(300)     // 6, 3%
                setPercent.add(400)     // 7, 4%
                setPercent.add(500)     // 8, 5%
                setPercent.add(500)     // 9, 5%
                setPercent.add(2000)    // 10, 20%
                setPercent.add(1500)    // 11, 15%
                setPercent.add(1100)    // 12, 11%
                setPercent.add(550)     // 13, 5.5%
                setPercent.add(450)     // 14, 4.5%
                setPercent.add(400)     // 15, 4%
                setPercent.add(350)     // 16, 3.5%
                setPercent.add(300)     // 17, 3%
                setPercent.add(250)     // 18, 2.5%
                setPercent.add(200)     // 19, 2%
                setPercent.add(150)     // 20, 1.5%
                setPercent.add(100)     // 21, 1%
                setPercent.add(90)      // 22, 0.9%
                setPercent.add(80)      // 23, 0.8%
                setPercent.add(70)      // 24, 0.7%
                setPercent.add(60)      // 25, 0.6%
                setPercent.add(50)      // 26, 0.5%
                setPercent.add(40)      // 27, 0.4%
                setPercent.add(30)      // 28, 0.3%
                setPercent.add(20)      // 29, 0.2%
                setPercent.add(10)      // 30, 0.1%
            }
            GambleType.GAMBLE_100 -> {
                price = preferencesDTO.priceGamble100!!
                maxDiamond = 100
                setPercent.add(1)       // 1, 0.01%
                setPercent.add(2)       // 2, 0.02%
                setPercent.add(3)       // 3, 0.03%
                setPercent.add(4)       // 4, 0.04%
                setPercent.add(5)       // 5, 0.05%
                setPercent.add(6)       // 6, 0.06%
                setPercent.add(7)       // 7, 0.07%
                setPercent.add(8)       // 8, 0.08%
                setPercent.add(9)       // 9, 0.09%
                setPercent.add(10)      // 10, 0.1%
                setPercent.add(20)      // 11, 0.2%
                setPercent.add(30)      // 12, 0.3%
                setPercent.add(40)      // 13, 0.4%
                setPercent.add(50)      // 14, 0.5%
                setPercent.add(60)      // 15, 0.6%
                setPercent.add(70)      // 16, 0.7%
                setPercent.add(80)      // 17, 0.8%
                setPercent.add(90)      // 18, 0.9%
                setPercent.add(100)     // 19, 1%
                setPercent.add(110)     // 20, 1.1%
                setPercent.add(120)     // 21, 1.2%
                setPercent.add(130)     // 22, 1.3%
                setPercent.add(140)     // 23, 1.4%
                setPercent.add(150)     // 24, 1.5%
                setPercent.add(160)     // 25, 1.6%
                setPercent.add(170)     // 26, 1.7%
                setPercent.add(180)     // 27, 1.8%
                setPercent.add(190)     // 28, 1.9%
                setPercent.add(200)     // 29, 2%
                setPercent.add(1000)    // 30, 10%
                setPercent.add(900)     // 31, 9%
                setPercent.add(850)     // 32, 8.5%
                setPercent.add(734)     // 33, 7.34%
                setPercent.add(600)     // 34, 6%
                setPercent.add(500)     // 35, 5%
                setPercent.add(400)     // 36, 4%
                setPercent.add(300)     // 37, 3%
                setPercent.add(200)     // 38, 2%
                setPercent.add(100)     // 39, 1%
                setPercent.add(99)      // 40, 0.99%
                setPercent.add(98)      // 41, 0.98%
                setPercent.add(97)      // 42, 0.97%
                setPercent.add(96)      // 43, 0.96%
                setPercent.add(95)      // 44, 0.95%
                setPercent.add(94)      // 45, 0.94%
                setPercent.add(93)      // 46, 0.93%
                setPercent.add(92)      // 47, 0.92%
                setPercent.add(91)      // 48, 0.91%
                setPercent.add(90)      // 49, 0.9%
                setPercent.add(51)      // 50, 0.51%
                setPercent.add(50)      // 51, 0.5%
                setPercent.add(49)      // 52, 0.49%
                setPercent.add(48)      // 53, 0.48%
                setPercent.add(47)      // 54, 0.47%
                setPercent.add(46)      // 55, 0.46%
                setPercent.add(45)      // 56, 0.45%
                setPercent.add(44)      // 57, 0.44%
                setPercent.add(43)      // 58, 0.43%
                setPercent.add(42)      // 59, 0.42%
                setPercent.add(41)      // 60, 0.41%
                setPercent.add(40)      // 61, 0.4%
                setPercent.add(39)      // 62, 0.39%
                setPercent.add(38)      // 63, 0.38%
                setPercent.add(37)      // 64, 0.37%
                setPercent.add(36)      // 65, 0.36%
                setPercent.add(35)      // 66, 0.35%
                setPercent.add(34)      // 67, 0.34%
                setPercent.add(33)      // 68, 0.33%
                setPercent.add(32)      // 69, 0.32%
                setPercent.add(31)      // 70, 0.31%
                setPercent.add(30)      // 71, 0.3%
                setPercent.add(29)      // 72, 0.29%
                setPercent.add(28)      // 73, 0.28%
                setPercent.add(27)      // 74, 0.27%
                setPercent.add(26)      // 75, 0.26%
                setPercent.add(25)      // 76, 0.25%
                setPercent.add(24)      // 77, 0.24%
                setPercent.add(23)      // 78, 0.23%
                setPercent.add(22)      // 79, 0.22%
                setPercent.add(21)      // 80, 0.21%
                setPercent.add(20)      // 81, 0.2%
                setPercent.add(19)      // 82, 0.19%
                setPercent.add(18)      // 83, 0.18%
                setPercent.add(17)      // 84, 0.17%
                setPercent.add(16)      // 85, 0.16%
                setPercent.add(15)      // 86, 0.15%
                setPercent.add(14)      // 87, 0.14%
                setPercent.add(13)      // 88, 0.13%
                setPercent.add(12)      // 89, 0.12%
                setPercent.add(11)      // 90, 0.11%
                setPercent.add(10)      // 91, 0.1%
                setPercent.add(9)       // 92, 0.09%
                setPercent.add(8)       // 93, 0.08%
                setPercent.add(7)       // 94, 0.07%
                setPercent.add(6)       // 95, 0.06%
                setPercent.add(5)       // 96, 0.05%
                setPercent.add(4)       // 97, 0.04%
                setPercent.add(3)       // 98, 0.03%
                setPercent.add(2)       // 99, 0.02%
                setPercent.add(1)       // 100, 0.01%

            }
        }

        val resultValue = getValue(setPercent)
        var percent = ((resultValue.toDouble()!! / setPercent.size!!) * 100).toInt()
        binding.textCelebrate.visibility = View.GONE
        when (percent) {
            in 0..20 -> {
                Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.gamble_result_back_1).fitCenter().into(binding.imgBackground)
                (binding.imgDiamond.background as AnimationDrawable).selectDrawable(0)
            }
            in 21..40 -> {
                Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.gamble_result_back_2).fitCenter().into(binding.imgBackground)
                (binding.imgDiamond.background as AnimationDrawable).selectDrawable(1)
            }
            in 41..60 -> {
                Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.gamble_result_back_3).fitCenter().into(binding.imgBackground)
                (binding.imgDiamond.background as AnimationDrawable).selectDrawable(3)
            }
            in 61..80 -> {
                Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.gamble_result_back_4).fitCenter().into(binding.imgBackground)
                (binding.imgDiamond.background as AnimationDrawable).selectDrawable(2)
            }
            in 81..100 -> {
                Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.gamble_result_back_5).fitCenter().into(binding.imgBackground)
                binding.textCelebrate.visibility = View.VISIBLE
                (binding.imgDiamond.background as AnimationDrawable).selectDrawable(6)
            }
            else -> {
                Glide.with(binding.imgBackground.context).asBitmap().load(R.drawable.gamble_result_back_5).fitCenter().into(binding.imgBackground)
                (binding.imgDiamond.background as AnimationDrawable).selectDrawable(1)
            }
        }

        //binding.imgDiamond.setImageResource(R.drawable.diamond_pack5)
        binding.textResult.text = "$resultValue"
        binding.layoutResult.visibility = View.VISIBLE
        //val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        //binding.layoutResult.startAnimation(fadeIn)

        val fadeIn = ObjectAnimator.ofFloat(binding.layoutResult, "alpha", 0f, 1f)
        fadeIn.duration = 2000
        fadeIn.start()
        fadeIn.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animator: Animator?) {

            }

            override fun onAnimationEnd(animator: Animator?) {
                mIsBusy = false
                binding.buttonGambleFinish.visibility = View.VISIBLE
            }

            override fun onAnimationCancel(animator: Animator?) {

            }

            override fun onAnimationStart(animator: Animator?) {

            }
        })

        // 다이아 차감
        var user = (activity as MainActivity?)?.getUser()!!
        val oldPaidGemCount = user.paidGem!!
        val oldFreeGemCount = user.freeGem!!
        firebaseViewModel.useUserGem(user.uid.toString(), price!!) { userDTO ->
            if (userDTO != null) {
                var log = LogDTO("[다이아 차감] ${maxDiamond}다이아 뽑기로 ${price} 다이아 사용 (paidGem : $oldPaidGemCount -> ${userDTO?.paidGem}, freeGem : $oldFreeGemCount -> ${userDTO?.freeGem})", Date())
                firebaseViewModel.writeUserLog(user?.uid.toString(), log) { }

                firebaseViewModel.addUserGem(user?.uid.toString(), 0, resultValue) { userDTO2->
                    if (userDTO2 != null) {
                        println("뽑기 $userDTO2")
                        firebaseViewModel.updateTodayCompleteGambleCount(user?.uid.toString()) { count->
                            if (count != null) {
                                mGambleCompleteCount = count
                                showGambleCount { }
                            }
                        }
                        var log = LogDTO("[다이아 추가] ${maxDiamond}다이아 뽑기로 ${resultValue} 다이아 추가 (paidGem : ${userDTO?.paidGem} -> ${userDTO2?.paidGem}, freeGem : ${userDTO?.freeGem} -> ${userDTO2?.freeGem})", Date())
                        firebaseViewModel.writeUserLog(user?.uid.toString(), log) { }
                    }
                }
            }
        }

        /*//var map = mutableMapOf<Int, Int>(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0, 6 to 0, 7 to 0, 8 to 0, 9 to 0, 10 to 0)
        var map = mutableMapOf<Int, Int>()
        for (i in 1..setPercent.size) {
            map[i] = 0
        }

        val gambleCount = 100000
        var getCount = 0
        for (i in 0 until gambleCount) {
            val resultValue = getValue(setPercent)
            map[resultValue] = map[resultValue]!!.plus(1)
            getCount = getCount.plus(resultValue)
        }
        var re = ""
        for (m in map) {
            var percent = ((m.value?.toDouble()!! / 100000!!) * 100).toDouble()
            re += "${m.key} 다이아 - ${m.value}(${percent}%)\n"
        }
        //binding.textTest.text = re
        println(re)
        println("${maxDiamond}뽑기 횟수 : $gambleCount, 최종 소모 다이아 : ${gambleCount.times(price)}, 최종 획득 다이아 : ${getCount}")*/
    }

    private fun getValue(setPercent : ArrayList<Int>) : Int {
        var totalValue = 0
        var percentValue = 0
        var valuePair = arrayListOf<Pair<Int,Int>>()
        for (setValue in setPercent) {
            valuePair.add(Pair(percentValue.plus(1), percentValue.plus(setValue)))
            percentValue = percentValue.plus(setValue)
            totalValue = totalValue.plus(setValue)
        }
        //println("확률 총합 : $totalValue")

        var resultValue = 0
        val randomValue = Random.nextInt(1, 10001)
        for (index in 0 until valuePair.size) {
            if (randomValue in valuePair[index].first..valuePair[index].second) {
                resultValue = index.plus(1)
                break
            }
        }

        return resultValue
    }

    private fun callToast(message: String) {
        if (toast == null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        } else {
            toast?.setText(message)
        }
        toast?.show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentGamble.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentGamble().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}