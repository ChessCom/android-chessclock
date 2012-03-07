package com.chess.activities;


import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;

import java.util.ArrayList;
import java.util.List;

/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class GamePanelTestActivity extends Activity implements View.OnClickListener {

    private LinearLayout whiteCapturedPieces;
    private LinearLayout blackCapturedPieces;
    private ListView movesListView;

//    private static final int ITEMS_CNT = 4;
    private static final float WEIGHT_SUM = 16f;

    private LinearLayout weightView;
    private LinearLayout seekView;
    private LinearLayout changeView;
    private TextView total;
    private WeightHelper helper;
    
    private int currentItemsCnt;
    
    

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_panel);
        whiteCapturedPieces = (LinearLayout) findViewById(R.id.whiteCapturedPieces);
        blackCapturedPieces = (LinearLayout) findViewById(R.id.blackCapturedPieces);
//		whiteCapturedPieces.setWeightSum(WEIGHT_SUM);
//		blackCapturedPieces.setWeightSum(WEIGHT_SUM);
        movesListView = (ListView) findViewById(R.id.movesListView);

        weightView = (LinearLayout) findViewById(R.id.weightView);
        seekView = (LinearLayout) findViewById(R.id.seekView);
        changeView = (LinearLayout) findViewById(R.id.changeView);
        
        
        findViewById(R.id.addBtn).setOnClickListener(this);
        findViewById(R.id.removeBtn).setOnClickListener(this);

        List<String> itemList = new ArrayList<String>();

        itemList.add("1111");
        itemList.add("1111");
        itemList.add("1111");
        itemList.add("1111");

        movesListView.setAdapter(new MovesAdapter(itemList));


//        addItems(whiteCapturedPieces, R.drawable.tap_1, 1, 0.6f);
//        addItems(whiteCapturedPieces, R.drawable.tap_2, 2, 1.2f);
//        addItems(whiteCapturedPieces, R.drawable.tap_6, 2, 1.2f);
//        addItems(whiteCapturedPieces, R.drawable.tap_3, 2, 1.2f);
//        addItems(whiteCapturedPieces, R.drawable.tap_4, 8, 1.2f);
//        addItems(whiteCapturedPieces, R.drawable.tap_5, 1, 0.6f);
//
//        addItems(blackCapturedPieces, R.drawable.tap_1, 1, 1.0f);
//        addItems(blackCapturedPieces, R.drawable.tap_2, 2, 1.0f);
//        addItems(blackCapturedPieces, R.drawable.tap_6, 2, 1.0f);
//        addItems(blackCapturedPieces, R.drawable.tap_3, 2, 1.0f);
//        addItems(blackCapturedPieces, R.drawable.tap_4, 6, 1.0f);
//        addItems(blackCapturedPieces, R.drawable.tap_5, 1, 1.0f);
//        addItems(whiteCapturedPieces, R.drawable.tap_1, 1, 0.6f);

        addItems(whiteCapturedPieces, R.drawable.captured_bq, 1, 1.0f);
        addItems(whiteCapturedPieces, R.drawable.captured_br, 2, 1.0f);
        addItems(whiteCapturedPieces, R.drawable.captured_bb, 2, 1.0f);
        addItems(whiteCapturedPieces, R.drawable.captured_bn, 2, 1.0f);
        addItems(whiteCapturedPieces, R.drawable.captured_bp, 8, 1.0f);
        addItems(whiteCapturedPieces, R.drawable.captured_bk, 1, 1.0f);

        addItems(blackCapturedPieces, R.drawable.captured_wq, 1, 1.0f);
        addItems(blackCapturedPieces, R.drawable.captured_wr, 2, 1.0f);
        addItems(blackCapturedPieces, R.drawable.captured_wb, 2, 1.0f);
        addItems(blackCapturedPieces, R.drawable.captured_wn, 2, 1.0f);
        addItems(blackCapturedPieces, R.drawable.captured_wp, 8, 1.0f);
        addItems(blackCapturedPieces, R.drawable.captured_wk, 1, 1.0f);
        
        helper = new WeightHelper();



        weightView.setWeightSum(100);

//        for(int k = 0; k<ITEMS_CNT; k++){
        currentItemsCnt = 6;
            addWeightElement();
            addWeightElement();
            addWeightElement();
            addWeightElement();
            addWeightElement();
            addWeightElement();
//        }

        total = new TextView(this);
        total.setTextColor(Color.RED);
        total.setText("total weightSum = " + helper.getTotalWeightSum());
        changeView.addView(total,0);
        weightView.setWeightSum(helper.getTotalWeightSum());
    }

    private void addWeightElement(){
//        currentItemsCnt++;
        WeightItem item = new WeightItem();
        item.position = helper.itemsList.size();
        item.weight = 100/currentItemsCnt;
        helper.itemsList.add(item);

        TextView text = new TextView(this);
        text.setText("pos" + item.position);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.weight = item.weight;
        text.setLayoutParams(params);
        text.setId(0x00001000 + item.position);
        text.setBackgroundColor(Color.rgb(0xFF / (item.position + 1), 0x7F / (item.position + 1), 0xFF / (item.position + 1)));
        weightView.addView(text);


        // Liean container

        TextView seekText = new TextView(this);
        seekText.setText("pos " + item.position);
        seekText.setPadding(0,0,10,0);
        SeekBar seekBar = new SeekBar(this);
        seekBar.setId(0x00001000 + item.position + 30);
        seekBar.setMax(100/currentItemsCnt);
        seekBar.setOnSeekBarChangeListener(new SeekListener(item));
        seekBar.setPadding(0,5,0,5);
        seekBar.setProgress(seekBar.getMax());

        LinearLayout linearLayout = new LinearLayout(this);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT);
        seekBar.setLayoutParams(params);
        linearLayout.addView(seekText);
        linearLayout.addView(seekBar);


        seekView.addView(linearLayout);

        TextView ttt = new TextView(this);

        ttt.setId(0x00002000 + item.position);
        ttt.setText("weight of item " + item.position + " w = " + item.weight);
        changeView.addView(ttt);
    }

    private void removeWeightElement() {
        currentItemsCnt--;
        seekView.removeAllViews();
        weightView.removeAllViews();
        changeView.removeAllViews();
        weightView.invalidate();
        changeView.invalidate();
        seekView.invalidate();

        helper.itemsList.clear();

        int dd = currentItemsCnt;
        for(int r=0 ;r<dd; r++){
            addWeightElement();
        }
        total = new TextView(this);
        total.setTextColor(Color.RED);
        total.setText("total weightSum = " + helper.getTotalWeightSum());
        changeView.addView(total,0);

        weightView.invalidate();
        changeView.invalidate();
        seekView.invalidate();
    }


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.addBtn){
            addWeightElement();
        }else if(view.getId() == R.id.removeBtn){
            removeWeightElement();
        }
    }


    private class SeekListener implements SeekBar.OnSeekBarChangeListener{

        private WeightItem item;

        public SeekListener(WeightItem item) {
            this.item = item;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            if(!b)
                return;

            float diff = item.weight - progress; 
            item.weight = progress;
            item.isTouched = true;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.weight = progress;
            findViewById(0x00001000 + item.position).setLayoutParams(params);


            helper.recountWeights(item.position,diff);
            //change weight of other elements

            
            // update other views
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            for (WeightItem weightItem : helper.itemsList) {
                TextView weTxt = (TextView) findViewById(0x00002000 + weightItem.position);
                weTxt.setText("weight of item " + weightItem.position + " w = " + weightItem.weight);
                if(weightItem.position == item.position){
                    continue;
                }

                params1.weight = weightItem.weight;
                findViewById(0x00001000 + weightItem.position).setLayoutParams(params1);

            }

//            TextView weightOfItemTxt = (TextView) findViewById(0x00002000 + item.position);
//            weightOfItemTxt.setText("weight of item " + item.position + " w = " + item.weight);

            total.setText("total weightSum = " + helper.getTotalWeightSum());

            
            
            
            weightView.invalidate();
            changeView.invalidate();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    private class WeightItem{
        public int position;
//        public int totalItems;
        public float weight;
        public boolean isTouched;
    }
    
    
    
    private class WeightHelper{
        
        public List<WeightItem> itemsList;
        public float maxWeight;
        public int totalItems;
        private float totalWeightSum;

        public WeightHelper(){
            itemsList = new ArrayList<WeightItem>();
        }

        public float weightCnt(int itemPosition,float itemPercentSize){
            float itemWeight = maxWeight / totalItems;                        
            return 0;
        }

        public float getTotalWeightSum() {
            totalWeightSum = 0;
            for (WeightItem weightItem : itemsList) {
                totalWeightSum += weightItem.weight;
            }

            return totalWeightSum;
        }

        public void recountWeights(int immutablePosition, float diff) {
            int items2change = 0;
            for (WeightItem weightItem : itemsList) {
                if(!weightItem.isTouched)
                    items2change++;
            }

            float weightDiff = diff/items2change;
            for (int i = 0, itemsListSize = itemsList.size(); i < itemsListSize; i++) {
                if(i== immutablePosition){
                    continue;
                }
                WeightItem weightItem = itemsList.get(i);
                if(weightItem.isTouched)
                    continue;
                weightItem.weight += weightDiff;

            }
            
        }
    }
    

    private void addItems(LinearLayout viewGroup, int pieceId, int layersCnt, float itemWeight) {

        Drawable[] layers = new Drawable[layersCnt];

        for (int j = 0; j < layersCnt; j++) {
            layers[j] = getResources().getDrawable(pieceId);
        }

        LayerDrawable pieceDrawable = new LayerDrawable(layers);

        for (int i = 0; i < layersCnt; i++) {
            shiftLayer(pieceDrawable, i);
        }

        ImageView imageView = new ImageView(this);
        imageView.setAdjustViewBounds(false);
        imageView.setScaleType(ImageView.ScaleType.CENTER);


        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        params.weight = itemWeight;
        params.gravity = Gravity.LEFT;

        imageView.setImageDrawable(pieceDrawable);
        imageView.setLayoutParams(imageParams);
        viewGroup.setWeightSum(16f);

        // put iamge inside frame to get left gravity
        FrameLayout frame = new FrameLayout(this);
        frame.addView(imageView);
        frame.setLayoutParams(params);

        viewGroup.addView(frame);
        viewGroup.setGravity(Gravity.LEFT);
    }

    private int shiftSize = 5;

    private void shiftLayer(LayerDrawable pieceDrawable, int level) {

        int l = level * shiftSize;
        int r = 0;
        int t = 0;
        int b = 0;
        pieceDrawable.setLayerInset(level, l, t, r, b);
        ((BitmapDrawable) pieceDrawable.getDrawable(level)).setGravity(Gravity.LEFT | Gravity.TOP);
    }


    private class MovesAdapter extends BaseAdapter {

        private List<String> itemList;

        public MovesAdapter(List<String> itemList) {
            this.itemList = itemList;
        }

        @Override
        public int getCount() {
            return itemList.size();  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Object getItem(int i) {
            return itemList.get(i);  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public long getItemId(int i) {
            return i;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView textView = new TextView(getApplicationContext());
            textView.setText(itemList.get(i));
            return textView;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}