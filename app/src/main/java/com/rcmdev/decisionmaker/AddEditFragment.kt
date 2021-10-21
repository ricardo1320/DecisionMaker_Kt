package com.rcmdev.decisionmaker

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.rcmdev.decisionmaker.adapters.OptionsAdapter
import com.rcmdev.decisionmaker.databinding.FragmentAddEditBinding
import com.rcmdev.decisionmaker.models.Roulette

private const val MAX_OPTIONS = 20
private const val MIN_OPTIONS = 2
private const val ARG_ROULETTE = "roulette"
private lateinit var optionsAdapter: OptionsAdapter

/**
 * A simple [Fragment] subclass.
 * Use the [AddEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEditFragment : Fragment(), View.OnClickListener {
    private var roulette: Roulette? = null
    private var listener: OnSaveClicked? = null
    private var _binding: FragmentAddEditBinding? = null
    private val binding get() = _binding!!
    private lateinit var toast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roulette = arguments?.getParcelable(ARG_ROULETTE)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var optionList = ArrayList<String>()
        val roulette = roulette

        if(roulette != null){
            binding.editTextRouletteTitle.setText(roulette.name)
            optionList = roulette.options
        }

        optionsAdapter = OptionsAdapter(optionList)
        binding.optionsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.optionsRecyclerView.adapter = optionsAdapter

        binding.editTextAddOption.setOnKeyListener(object:View.OnKeyListener{
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if((event?.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    if(binding.editTextAddOption.text.isNotBlank()){
                        if(optionsAdapter.itemCount < MAX_OPTIONS){
                            optionsAdapter.addOption(binding.editTextAddOption.text.trim().toString())
                            binding.editTextAddOption.text.clear()
                            binding.editTextAddOption.requestFocus()
                            return true
                        }else{
                            cancelToast()
                            toast = Toast.makeText(activity, resources.getString(R.string.MAX_OPTIONS_CONSTRAINT), Toast.LENGTH_LONG)
                            toast.show()
                        }
                    }
                }
                return false
            }
        })

        binding.buttonAddOption.setOnClickListener(this)
        binding.buttonClearTitle.setOnClickListener(this)
        binding.floatButtonReady.setOnClickListener(this)
    }

    override fun onPrepareOptionsMenu(menu: Menu) { menu.clear() }

    override fun onClick(v: View) {
        when(v.id){
            R.id.button_addOption -> {
                if(binding.editTextAddOption.text.isNotBlank()){
                    if(optionsAdapter.itemCount < MAX_OPTIONS){
                        optionsAdapter.addOption(binding.editTextAddOption.text.trim().toString())
                        binding.editTextAddOption.text.clear()
                    }else{
                        cancelToast()
                        toast = Toast.makeText(activity, resources.getString(R.string.MAX_OPTIONS_CONSTRAINT), Toast.LENGTH_LONG)
                        toast.show()
                    }
                }
            }
            R.id.button_clearTitle -> { binding.editTextRouletteTitle.text.clear() }
            R.id.floatButton_ready -> { checkMinOptions() }
        }
    }

    private fun cancelToast(){
        if (this::toast.isInitialized) toast.cancel()
    }

    private fun rouletteFromUI(): Roulette {
        val optionList = optionsAdapter.getOptions()
        return Roulette(binding.editTextRouletteTitle.text.toString(), optionList)
    }

    private fun checkMinOptions(){
        if(optionsAdapter.itemCount < MIN_OPTIONS) {
            cancelToast()
            toast = Toast.makeText(activity, resources.getString(R.string.MIN_OPTIONS_CONSTRAINT), Toast.LENGTH_LONG)
            toast.show()
        }else{
            if(binding.editTextRouletteTitle.text.isBlank())
                binding.editTextRouletteTitle.setText(resources.getString(R.string.ROULETTE_DEFAULT_TITLE))
            listener?.onSaveClicked(rouletteFromUI())
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is OnSaveClicked){
            listener = context
        }else{
            throw RuntimeException("$context must implement OnSavedClicked")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnSaveClicked{
        fun onSaveClicked(roulette: Roulette)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param roulette The roulette to be edited, or null to add a new roulette.
         * @return A new instance of fragment AddEditFragment.
         */
        @JvmStatic
        fun newInstance(roulette: Roulette?) =
            AddEditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ROULETTE, roulette)
                }
            }
    }
}