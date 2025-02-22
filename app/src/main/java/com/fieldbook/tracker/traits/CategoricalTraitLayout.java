package com.fieldbook.tracker.traits;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.fieldbook.tracker.R;
import com.fieldbook.tracker.activities.CollectActivity;
import com.fieldbook.tracker.preferences.GeneralKeys;
import com.fieldbook.tracker.utilities.CategoryJsonUtil;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;

import org.brapi.v2.model.pheno.BrAPIScaleValidValuesCategories;

import java.util.ArrayList;

public class CategoricalTraitLayout extends BaseTraitLayout {

    //todo this can eventually be merged with multicattraitlayout when we can support a switch in traits on how many categories to allow user to select

    public static String[] POSSIBLE_VALUES = new String[]{ "qualitative", "categorical" };

    //private StaggeredGridView gridMultiCat;
    private RecyclerView gridMultiCat;

    public CategoricalTraitLayout(Context context) {
        super(context);
    }

    public CategoricalTraitLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CategoricalTraitLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setNaTraitsText() {
    }

    @Override
    public String type() {
        return "categorical";
    }

    public boolean isTraitType(String trait) {
        return trait.equals("categorical") || trait.equals("qualitative");
    }

    @Override
    public void init() {
        gridMultiCat = findViewById(R.id.catGrid);
    }

    @Override
    public void loadLayout() {
        super.loadLayout();

        final String trait = getCurrentTrait().getTrait();
        getEtCurVal().setHint("");
        getEtCurVal().setVisibility(EditText.VISIBLE);

        //read the preferences, default to displaying values instead of labels
        String labelValPref = getPrefs().getString(GeneralKeys.LABELVAL_CUSTOMIZE,"value");

        //read the json object stored in additional info of the trait object (only in BrAPI imported traits)
        ArrayList<BrAPIScaleValidValuesCategories> cats = new ArrayList<>();

        String categoryString = getCurrentTrait().getCategories();
        try {

            cats = CategoryJsonUtil.Companion.decode(categoryString);

        } catch (Exception e) {

            String[] cat = categoryString.split("/");
            for (String label : cat) {
                BrAPIScaleValidValuesCategories c = new BrAPIScaleValidValuesCategories();
                c.setLabel(label);
                c.setValue(label);
                cats.add(c);
            }
        }

        if (!getNewTraits().containsKey(trait)) {

            getEtCurVal().setText("");

            getEtCurVal().setTextColor(Color.BLACK);

        } else {

            //if there is a saved value, check if its json or old string
            String savedJson = getNewTraits().get(trait);

            if (savedJson != null) {

                //check if its the new json
                try {

                    ArrayList<BrAPIScaleValidValuesCategories> c = CategoryJsonUtil.Companion.decode(savedJson);

                    if (!c.isEmpty()) {

                        //get the value from the single-sized array
                        BrAPIScaleValidValuesCategories labelVal = c.get(0);

                        //check that this pair is a valid label/val pair in the category,
                        //if it is then set the text based on the preference
                        if (CategoryJsonUtil.Companion.contains(cats, labelVal)) {

                            //display the category based on preferences
                            if (labelValPref.equals("value")) {

                                getEtCurVal().setText(labelVal.getValue());

                            } else {

                                getEtCurVal().setText(labelVal.getLabel());

                            }

                            getEtCurVal().setTextColor(Color.parseColor(getDisplayColor()));

                        }
                    }

                } catch (Exception e) {

                    e.printStackTrace(); //if it fails to decode, assume its an old string

                    if (CategoryJsonUtil.Companion.contains(cats, savedJson)) {

                        getEtCurVal().setText(savedJson);

                        getEtCurVal().setTextColor(Color.parseColor(getDisplayColor()));
                    }
                }
            }
        }

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setAlignItems(AlignItems.STRETCH);
        gridMultiCat.setLayoutManager(layoutManager);

        if (!((CollectActivity) getContext()).isDataLocked()) {

            ArrayList<BrAPIScaleValidValuesCategories> finalCats = cats;
            gridMultiCat.setAdapter(new CategoryTraitAdapter(getContext()) {

                @Override
                public void onBindViewHolder(CategoryTraitViewHolder holder, int position) {
                    holder.bindTo();

                    //get the label for this position
                    BrAPIScaleValidValuesCategories pair = finalCats.get(position);

                    //update button with the preference based text
                    if (labelValPref.equals("value")) {

                        holder.mButton.setText(pair.getValue());

                    } else {

                        holder.mButton.setText(pair.getLabel());

                    }

                    //set the buttons tag to the json, when clicked this is updated in db
                    holder.mButton.setTag(pair);
                    holder.mButton.setOnClickListener(createClickListener(holder.mButton, position));

                    //update the button's state if this category is selected
                    String currentText = getEtCurVal().getText().toString();

                    if (labelValPref.equals("value")) {

                        if (currentText.equals(pair.getValue())) {

                            pressOnButton(holder.mButton);

                        } else pressOffButton(holder.mButton);

                    } else {

                        if (currentText.equals(pair.getLabel())) {

                            pressOnButton(holder.mButton);

                        } else pressOffButton(holder.mButton);
                    }
                }

                @Override
                public int getItemCount() {
                    return finalCats.size();
                }
            });
        }

        gridMultiCat.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                gridMultiCat.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                gridMultiCat.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                View lastChild = gridMultiCat.getChildAt(gridMultiCat.getChildCount() - 1);
//                gridMultiCat.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, lastChild.getBottom()));
                gridMultiCat.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            }
        });
    }

    private OnClickListener createClickListener(final Button button, int position) {
        return v -> {

            if (!((CollectActivity) getContext()).isDataLocked()) {
                //cast tag to the buttons label/val pair
                final BrAPIScaleValidValuesCategories pair = (BrAPIScaleValidValuesCategories) button.getTag();
                final ArrayList<BrAPIScaleValidValuesCategories> scale = new ArrayList<>(); //this is saved in the db

                final String category = button.getText().toString();
                String currentCat = getEtCurVal().getText().toString(); //displayed in the edit text

                if (currentCat.equals(category)) {
                    pressOffButton(button);
                    currentCat = "";
                } else {
                    pressOnButton(button);
                    currentCat = category;
                    scale.add(pair);
                }

                getEtCurVal().setText(currentCat);

                updateTrait(getCurrentTrait().getTrait(),
                        getCurrentTrait().getFormat(),
                        CategoryJsonUtil.Companion.encode(scale));

                triggerTts(category);

                loadLayout(); //todo this is not the best way to do this
            }
        };
    }

    private void pressOnButton(Button button) {
        button.setTextColor(Color.parseColor(getDisplayColor()));
        button.getBackground().setColorFilter(button.getContext().getResources().getColor(R.color.button_pressed), PorterDuff.Mode.MULTIPLY);
    }

    private void pressOffButton(Button button) {
        button.setTextColor(Color.BLACK);
        button.getBackground().setColorFilter(button.getContext().getResources().getColor(R.color.button_normal), PorterDuff.Mode.MULTIPLY);
    }

    @Override
    public void deleteTraitListener() {
        ((CollectActivity) getContext()).removeTrait();
        loadLayout();
    }
}
