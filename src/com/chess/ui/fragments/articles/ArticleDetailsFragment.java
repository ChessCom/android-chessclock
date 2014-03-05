package com.chess.ui.fragments.articles;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.*;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.ArticleDetailsItem;
import com.chess.backend.entity.api.CommonCommentItem;
import com.chess.backend.entity.api.CommonViewedItem;
import com.chess.backend.entity.api.PostCommentItem;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.image_load.bitmapfun.DiagramImageProcessor;
import com.chess.backend.image_load.bitmapfun.ImageCache;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.model.GameDiagramItem;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.CommentsCursorAdapter;
import com.chess.ui.adapters.CustomSectionedAdapter;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.diagrams.GameDiagramFragment;
import com.chess.ui.fragments.diagrams.GameDiagramFragmentTablet;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.views.ControlledListView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.FontsHelper;
import com.chess.widgets.RoboTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.01.13
 * Time: 19:12
 */
public class ArticleDetailsFragment extends CommonLogicFragment implements ItemClickListenerFace {

    private static final int CONTENT_SECTION = 0;
    private static final int COMMENTS_SECTION = 1;
    private static final String IMAGE_CACHE_DIR = "diagrams";
    private static final String THUMBS_CACHE_DIR = "thumbs";

    public static final String ITEM_ID = "item_id";
    public static final String GREY_COLOR_DIVIDER = "##";
    public static final String P_TAG_OPEN = "<p>";
    public static final String P_TAG_CLOSE = "</p>";
    private static final long KEYBOARD_DELAY = 100;
    public static final int IMAGE_PREFIX = 0x0000A000;
    public static final int TEXT_PREFIX = 0x0000B000;
    public static final int ICON_PREFIX = 0x0000C000;

    // 11/15/12 | 27 min
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");
    public static final String SLASH_DIVIDER = " | ";
    private static final long NON_EXIST = -1;
    private static final long READ_DELAY = 2 * 1000;
    public static final String NO_ITEM_IMAGE = "no_item_image";
    public static final String DIAGRAM_START_TAG = "<!-- CHESS_COM_DIAGRAM";
    private static final int ID_POSITION = 1;
    public static final String CHESS_COM_DIAGRAM = "chess_com_diagram";
    public static float IMAGE_WIDTH_PERCENT = 0.80f;

    private TextView authorTxt;
    private TextView titleTxt;
    private ProgressImageView articleImg;
    private ProgressImageView authorImg;
    private ImageView countryImg;
    private TextView dateTxt;
    private TextView contentTxt;
    private long articleId;
    private CommentsUpdateListener commentsUpdateListener;
    private CommentsCursorAdapter commentsCursorAdapter;
    private ArticleUpdateListener articleUpdateListener;
    private CustomSectionedAdapter sectionedAdapter;
    private DiagramsAdapter diagramsAdapter;
    private int authorImageSize;
    private SparseArray<String> countryMap;
    private View replyView;
    private EditText newPostEdt;
    private int paddingSide;
    private CommentPostListener commentPostListener;
    private String url;
    private String bodyStr;
    private long commentId;
    private boolean inEditMode;
    private String commentForEditStr;
    private View loadingCommentsView;
    private List<ArticleDetailsItem.Diagram> diagramsList;
    private SparseBooleanArray simpleIdsMap;
    private int iconOverlaySize;
    private int iconOverlayColor;
    private int textColor;
    private int textSize;
    private DiagramImageProcessor diagramImageProcessor;
    private boolean diagramsLoaded;
    private List<DiagramListItem> contentPartsList;
    private SmartImageFetcher articleImageFetcher;
    private String authorImgUrl;
    private String articleImageUrl;
    private int infoTextSize;
    private CharSequence authorStr;
    private String titleStr;
    private int mainArticleImageWidth;

    public static ArticleDetailsFragment createInstance(long articleId) {
        ArticleDetailsFragment frag = new ArticleDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(ITEM_ID, articleId);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            articleId = getArguments().getLong(ITEM_ID);
        } else {
            articleId = savedInstanceState.getLong(ITEM_ID);
        }
        init();

        pullToRefresh(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.article_details_frame, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.articles);

        widgetsInit(view);

        // adjust action bar icons
        getActivityFace().showActionMenu(R.id.menu_edit, true);
        getActivityFace().showActionMenu(R.id.menu_share, true);
        getActivityFace().showActionMenu(R.id.menu_notifications, false);
        getActivityFace().showActionMenu(R.id.menu_games, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (need2update) {
            loadFromDb();
        } else {
            loadTextWithImage(titleTxt, titleStr, mainArticleImageWidth);
            authorTxt.setText(authorStr);
            if (bodyStr.contains(DIAGRAM_START_TAG)) {
                loadTextWithImage(contentTxt, bodyStr, mainArticleImageWidth);
            }

            // load main article image
            articleImageFetcher.loadImage(new SmartImageFetcher.Data(articleImageUrl, mainArticleImageWidth),
                    articleImg.getImageView());
            // load avatar of author
            articleImageFetcher.loadImage(new SmartImageFetcher.Data(authorImgUrl, authorImageSize), authorImg.getImageView());
        }

        diagramImageProcessor.setExitTasksEarly(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(markAsReadRunnable);

        diagramImageProcessor.setPauseWork(false);
        diagramImageProcessor.setExitTasksEarly(true);
        diagramImageProcessor.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        diagramImageProcessor.closeCache();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(ITEM_ID, articleId);
    }

    private void loadFullBody() {
        if (isNetworkAvailable()) {
            // get full body text from server
            LoadItem loadItem = new LoadItem();
            loadItem.setLoadPath(RestHelper.getInstance().CMD_ARTICLE_BY_ID(articleId));

            new RequestJsonTask<ArticleDetailsItem>(articleUpdateListener).executeTask(loadItem);
        }
    }

    private void loadFromDb() {
        Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getArticleById(articleId));

        if (cursor.moveToFirst()) { // we definitely have record in DB about this article
            int lightGrey = getResources().getColor(R.color.new_subtitle_light_grey);
            String firstName = DbDataManager.getString(cursor, DbScheme.V_FIRST_NAME);
            CharSequence chessTitle = DbDataManager.getString(cursor, DbScheme.V_CHESS_TITLE);
            String lastName = DbDataManager.getString(cursor, DbScheme.V_LAST_NAME);
            if (TextUtils.isEmpty(chessTitle)) {
                authorStr = firstName + Symbol.SPACE + lastName;
            } else {
                authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER
                        + Symbol.SPACE + firstName + Symbol.SPACE + lastName;
                authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, new ForegroundColorSpan(lightGrey));
            }
            authorTxt.setText(authorStr);

            try {
                url = cursor.getString(cursor.getColumnIndexOrThrow(DbScheme.V_URL));
            } catch (IllegalArgumentException ex) {
                url = Symbol.EMPTY;
            }
            titleStr = DbDataManager.getString(cursor, DbScheme.V_TITLE);
            loadTextWithImage(titleTxt, titleStr, mainArticleImageWidth);

            authorImgUrl = DbDataManager.getString(cursor, DbScheme.V_USER_AVATAR);
            articleImageFetcher.loadImage(new SmartImageFetcher.Data(authorImgUrl, authorImageSize), authorImg.getImageView());

            articleImageUrl = DbDataManager.getString(cursor, DbScheme.V_PHOTO_URL);

            if (!isTablet) {
                // Change main article Image params
                mainArticleImageWidth = screenWidth;
                int imageHeight = (int) (screenWidth * 0.95f); // wide aspect is 0.6671f
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, imageHeight);
                articleImg.setLayoutParams(params);
            } else {
                mainArticleImageWidth = getResources().getDimensionPixelSize(R.dimen.article_detail_image_width);
            }

            // Change ProgressBar params
            RelativeLayout.LayoutParams progressParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            progressParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            articleImg.getProgressBar().setLayoutParams(progressParams);

            if (articleImageUrl.contains(NO_ITEM_IMAGE)) {
                articleImageUrl = authorImgUrl;
            }

            articleImageFetcher.loadImage(new SmartImageFetcher.Data(articleImageUrl, mainArticleImageWidth), articleImg.getImageView());

            Drawable drawable = AppUtils.getCountryFlagScaled(getActivity(), countryMap.get(DbDataManager.getInt(cursor, DbScheme.V_COUNTRY_ID)));
            countryImg.setImageDrawable(drawable);

            long createDate = DbDataManager.getLong(cursor, DbScheme.V_CREATE_DATE) * 1000L;
            dateTxt.setText(dateFormatter.format(new Date(createDate)));
            bodyStr = DbDataManager.getString(cursor, DbScheme.V_BODY);
            loadTextWithImage(contentTxt, bodyStr, mainArticleImageWidth);

            diagramsList = DbDataManager.getArticleDiagramItemFromDb(getContentResolver(), getUsername());

            // start loading diagrams here
            if (!loadDiagramsFromContent(bodyStr, diagramsList)) {
                loadFullBody();
            }
        }
    }

    /**
     * @return {@code true} if diagrams exist
     */
    private boolean loadDiagramsFromContent(String bodyStr, List<ArticleDetailsItem.Diagram> diagramList) {
        if (bodyStr.contains(DIAGRAM_START_TAG)) {

            // we go through the article body and divide it to parts
            String[] contentParts = bodyStr.split(DIAGRAM_START_TAG);
            // hide simple container
            contentTxt.setVisibility(View.GONE);

            for (String contentPart : contentParts) {
                DiagramListItem diagramListItem = new DiagramListItem();
                if (contentPart.contains(CHESS_COM_DIAGRAM)) {
                    // get diagramId
                    String diagramPart = contentPart.substring(contentPart.indexOf("<div "));
                    String partAfterDiagram = diagramPart.substring(diagramPart.indexOf("</div>") + "</div>".length());

                    String diagramIdStr = diagramPart.substring(diagramPart.indexOf("id=\"chess_com_diagram_")
                            + "id=\"chess_com_diagram_".length());
                    diagramIdStr = diagramIdStr.substring(0, diagramIdStr.indexOf("\" class"));
                    String[] diagramIdParts = diagramIdStr.split("_");

                    // set to item
                    diagramListItem.diagramId = Integer.parseInt(diagramIdParts[ID_POSITION]);

                    for (ArticleDetailsItem.Diagram diagram : diagramList) {
                        if (diagramListItem.diagramId == diagram.getDiagramId()) {
                            diagramListItem.diagram = diagram;
                            if (diagram.getType() == ArticleDetailsItem.Diagram.SIMPLE) {
                                simpleIdsMap.put(diagramListItem.diagramId, true);
                            } else {
                                simpleIdsMap.put(diagramListItem.diagramId, false);
                            }
                            break;
                        }
                    }
                    diagramListItem.textStr = partAfterDiagram;

                } else {
                    diagramListItem.textStr = contentPart;
                }
                contentPartsList.add(diagramListItem);
            }

            diagramsAdapter.setItemsList(contentPartsList);
            diagramsLoaded = true;
            need2update = false;
            updateComments();

            return true;
        } else {
            contentTxt.setVisibility(View.VISIBLE);
            updateComments();
            return false;
        }
    }

    @Override
    public void onRefreshStarted(View view) {
        super.onRefreshStarted(view);
        updateComments();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_cancel:
                showEditView(false);

                return true;
            case R.id.menu_accept:
                if (inEditMode) {
                    createPost(commentId);
                } else {
                    createPost();
                }
                return true;
            case R.id.menu_edit:
                showEditView(true);
                return true;
            case R.id.menu_share:
                String articleShareStr;
                if (TextUtils.isEmpty(url)) {
                    articleShareStr = String.valueOf(Html.fromHtml(bodyStr));
                } else {
                    articleShareStr = "http://chess.com/article/view/" + url;
                }

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this article - "
                        + Symbol.NEW_STR + articleShareStr);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        int id = view.getId();

        if (id == R.id.commentView) {
            Integer position = (Integer) view.getTag(R.id.list_item_id);
            Cursor cursor = (Cursor) commentsCursorAdapter.getItem(position);

            String username = DbDataManager.getString(cursor, DbScheme.V_USERNAME);
            if (username.equals(getUsername())) {
                commentId = DbDataManager.getLong(cursor, DbScheme.V_ID);

                commentForEditStr = String.valueOf(Html.fromHtml(DbDataManager.getString(cursor, DbScheme.V_BODY)));

                inEditMode = true;
                showEditView(true);
            }
        } else if (id == IMAGE_PREFIX || id == ICON_PREFIX) {
            //get diagramId from view tag
            Integer pos = (Integer) view.getTag(R.id.list_item_id);
            int diagramId = (int) diagramsAdapter.getItemId(pos);
            showDiagramAnimated(diagramId);
        }
    }

    private boolean showDiagramAnimated(final Integer diagramId) {
        // don't handle clicks on simple diagrams
        if (simpleIdsMap.get(diagramId)) {
            return false;
        }

        ArticleDetailsItem.Diagram diagramToShow = null;
        for (ArticleDetailsItem.Diagram diagram : diagramsList) {
            if (diagram.getDiagramId() == diagramId) {
                diagramToShow = diagram;
                break;
            }
        }

        final GameDiagramItem diagramItem = new GameDiagramItem();
        if (diagramToShow.getType() == ArticleDetailsItem.Diagram.PUZZLE) {
            diagramItem.setMovesList(diagramToShow.getMoveList());
            diagramItem.setDiagramType(ArticleDetailsItem.CHESS_PROBLEM);
        } else if (diagramToShow.getType() == ArticleDetailsItem.Diagram.CHESS_GAME) {
            diagramItem.setMovesList(diagramToShow.getMoveList());
            diagramItem.setDiagramType(ArticleDetailsItem.CHESS_GAME);
        } else if (diagramToShow.getType() == ArticleDetailsItem.Diagram.SIMPLE) {
            diagramItem.setDiagramType(ArticleDetailsItem.SIMPLE_DIAGRAM);
        }
        diagramItem.setFen(diagramToShow.getFen());
        diagramItem.setFlip(diagramToShow.getFlip());
        diagramItem.setFocusMove(diagramToShow.getFocusNode());

        if (!isTablet) {
            getActivityFace().openFragment(GameDiagramFragment.createInstance(diagramItem));
        } else {
            getActivityFace().openFragment(GameDiagramFragmentTablet.createInstance(diagramItem));
        }

        return true;
    }

    private Runnable markAsReadRunnable = new Runnable() {
        @Override
        public void run() {
            if (getActivity() == null) {
                return;
            }
            CommonViewedItem item = new CommonViewedItem(articleId, getUsername());
            DbDataManager.saveArticleViewedState(getContentResolver(), item);
        }
    };

    private void showEditView(boolean show) {
        if (show) {
            replyView.setVisibility(View.VISIBLE);
            replyView.setBackgroundResource(R.color.header_light);
            replyView.setPadding(paddingSide, paddingSide, paddingSide, paddingSide);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    newPostEdt.requestFocus();
                    showKeyBoard(newPostEdt);
                    showKeyBoardImplicit(newPostEdt);

                    if (inEditMode) {
                        newPostEdt.setText(commentForEditStr);
                        newPostEdt.setSelection(commentForEditStr.length());
                    }
                    showEditMode(true);
                }
            }, KEYBOARD_DELAY);
        } else {

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideKeyBoard(newPostEdt);
                    hideKeyBoard();

                    replyView.setVisibility(View.GONE);
                    newPostEdt.setText(Symbol.EMPTY);
                }
            }, KEYBOARD_DELAY);

            showEditMode(false);
            inEditMode = false;
        }
    }

    private void showEditMode(boolean show) {
        getActivityFace().showActionMenu(R.id.menu_share, !show);
        getActivityFace().showActionMenu(R.id.menu_edit, !show);
        getActivityFace().showActionMenu(R.id.menu_cancel, show);
        getActivityFace().showActionMenu(R.id.menu_accept, show);

        getActivityFace().updateActionBarIcons();
    }

    private class ArticleUpdateListener extends ChessUpdateListener<ArticleDetailsItem> {

        private ArticleUpdateListener() {
            super(ArticleDetailsItem.class);
        }

        @Override
        public void showProgress(boolean show) {
            showCommentsLoadingView(show);
        }

        @Override
        public void updateData(ArticleDetailsItem returnedObj) {
            super.updateData(returnedObj);

            ArticleDetailsItem.Data articleData = returnedObj.getData();
            int commentCount = articleData.getCommentCount();
            int viewCount = articleData.getViewCount();
            url = articleData.getUrl();

            String commentsCntStr = getString(R.string.comments_arg, commentCount);
            String viewsCntStr = getString(R.string.views_arg, viewCount);
            CharSequence text = dateTxt.getText();
            dateTxt.setText(text + SLASH_DIVIDER + viewsCntStr + SLASH_DIVIDER + commentsCntStr);

            diagramsList = articleData.getDiagrams();
            String bodyStr = articleData.getBody();
            if (!diagramsLoaded) {
                loadDiagramsFromContent(bodyStr, diagramsList);
            }

            loadTextWithImage(contentTxt, bodyStr, mainArticleImageWidth); // Shouldn't be used if complex view is used

            DbDataManager.saveArticleItem(getContentResolver(), articleData, true);

            if (diagramsList != null) {
                for (ArticleDetailsItem.Diagram diagram : diagramsList) {
                    DbDataManager.saveArticlesDiagramItem(getContentResolver(), diagram);
                }
            }
        }
    }

    private class DiagramListItem {
        String textStr;
        int diagramId;
        ArticleDetailsItem.Diagram diagram;
    }

    private class DiagramsAdapter extends ItemsAdapter<DiagramListItem> {

        private static final int DIAGRAM = 0;
        private static final int TEXT = 1;

        public DiagramsAdapter(Context context, List<DiagramListItem> itemList) {
            super(context, itemList);
        }

        @Override
        public int getItemViewType(int position) {
            if (itemsList.get(position).diagramId != 0) {
                return DIAGRAM;
            } else {
                return TEXT;
            }
        }

        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount() + 1;
        }

        @Override
        public long getItemId(int position) {
            return itemsList.get(position).diagramId;
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            if (view == null) {
                if (getItemViewType(pos) == DIAGRAM) {
                    view = createDiagramView(parent);
                } else {
                    view = createContentView(parent);
                }
            }
            bindView(itemsList.get(pos), pos, view);
            return view;
        }

        private View createDiagramView(ViewGroup parent) {
            DiagramViewHolder holder = new DiagramViewHolder();

            // create container for
            LinearLayout container = new LinearLayout(getActivity());
            AbsListView.LayoutParams containerParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;

            container.setOrientation(LinearLayout.VERTICAL);
            container.setLayoutParams(containerParams);

            {// add text info above diagram "Serper vs. Dorfman"
                RoboTextView textView = new RoboTextView(getActivity());
                textView.setTextSize(textSize);
                textView.setTextColor(textColor);
                textView.setPadding(paddingSide, 0, paddingSide, 0);
                textView.setFont(FontsHelper.BOLD_FONT);
                textView.setGravity(Gravity.CENTER);

                container.addView(textView, textLayoutParams);
                holder.playersTxt = textView;
            }

            {// add second line text info above diagram "Clock simul | Tashkent | 1983 | ECO: B63 | 1-0"
                RoboTextView textView = new RoboTextView(getActivity());
                textView.setTextSize(infoTextSize);
                textView.setTextColor(textColor);
                textView.setPadding(paddingSide, 0, paddingSide, 0);

                container.addView(textView, textLayoutParams);
                holder.infoTxt = textView;
            }

            // add FrameLayout for imageView and fragment container
            FrameLayout frameLayout = new FrameLayout(getActivity());
            int frameWidth = screenWidth;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(frameWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;

            frameLayout.setLayoutParams(params);

            {// add imageView with diagram bitmap
                // take 80% of screen width
                int imageSize = (int) (screenWidth * IMAGE_WIDTH_PERCENT);

                FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(imageSize, imageSize);
                imageParams.gravity = Gravity.CENTER;

                final ImageView imageView = new ImageView(getActivity());
                imageView.setLayoutParams(imageParams);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setId(IMAGE_PREFIX);

                // set click handler and then get this tag from view to show diagram
                imageView.setOnClickListener(ArticleDetailsFragment.this);

                holder.imageView = imageView;

                frameLayout.addView(imageView);

                imageView.setBackgroundResource(R.drawable.shadow_back_square);
            }

            container.addView(frameLayout);
            holder.fragmentContainer = frameLayout;

            // add icon for pop-out
            {
                FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                iconParams.gravity = Gravity.CENTER;

                final RoboTextView iconView = new RoboTextView(getActivity());
                iconView.setFont(FontsHelper.ICON_FONT);
                iconView.setText(R.string.ic_expand);
                iconView.setTextSize(iconOverlaySize);
                iconView.setTextColor(iconOverlayColor);
                iconView.setId(ICON_PREFIX);

                float shadowRadius = 1 * density + 0.5f;
                float shadowDx = 1 * density;
                float shadowDy = 1 * density;
                iconView.setShadowLayer(shadowRadius, shadowDx, shadowDy, 0x88000000);

                // set click handler and then get this tag from view to show diagram
                iconView.setOnClickListener(ArticleDetailsFragment.this);

                frameLayout.addView(iconView, iconParams);
                holder.iconView = iconView;
            }

            {// add "White to move"
                RoboTextView textView = new RoboTextView(getActivity());
                textView.setTextSize(textSize);
                textView.setTextColor(textColor);
                textView.setPadding(paddingSide, 0, paddingSide, 0);
                textView.setFont(FontsHelper.BOLD_FONT);

                container.addView(textView, textLayoutParams);
                holder.userToMoveTxt = textView;
            }

            {// add text content
                RoboTextView textView = new RoboTextView(getActivity(), null, R.attr.contentStyle);
                textView.setTextSize(textSize);
                textView.setTextColor(textColor);
                textView.setPadding(paddingSide, paddingSide, paddingSide, 0);
                textView.setId(TEXT_PREFIX);
                textView.setOnClickListener(ArticleDetailsFragment.this);
                textView.setMovementMethod(LinkMovementMethod.getInstance());

                container.addView(textView);
                holder.contentTxt = textView;
            }
            container.setTag(holder);

            return container;
        }

        private View createContentView(ViewGroup parent) {
            ContentViewHolder holder = new ContentViewHolder();

            RoboTextView textView = new RoboTextView(getActivity(), null, R.attr.contentStyle);
            textView.setTextSize(textSize);
            textView.setTextColor(textColor);
            textView.setPadding(paddingSide, paddingSide, paddingSide, 0);
            textView.setId(TEXT_PREFIX);
            textView.setOnClickListener(ArticleDetailsFragment.this);
            textView.setMovementMethod(LinkMovementMethod.getInstance());

            holder.contentTxt = textView;
            textView.setTag(holder);

            return textView;
        }

        @Override
        protected View createView(ViewGroup parent) {
            // not used here
            return null;
        }

        @Override
        protected void bindView(DiagramListItem item, int pos, View convertView) {

            int itemViewType = getItemViewType(pos);
            if (itemViewType == DIAGRAM) {

                DiagramViewHolder holder = (DiagramViewHolder) convertView.getTag();

                // use diagram data to create board image
                ArticleDetailsItem.Diagram diagramToShow = item.diagram;
                final GameDiagramItem diagramItem = new GameDiagramItem();
                diagramItem.setShowAnimation(false);
                if (diagramToShow.getType() == ArticleDetailsItem.Diagram.PUZZLE) {
                    diagramItem.setMovesList(diagramToShow.getMoveList());
                } else if (diagramToShow.getType() == ArticleDetailsItem.Diagram.CHESS_GAME) {
                    diagramItem.setMovesList(diagramToShow.getMoveList());
                }
                diagramItem.setFen(diagramToShow.getFen());
                diagramItem.setFlip(diagramToShow.getFlip());
                diagramItem.setFocusMove(diagramToShow.getFocusNode());

                // create board with pieces based on diagram
                View boardView = DiagramImageProcessor.createBoardView(diagramItem, getActivity());

                // get bitmap from fragmentView
                int bitmapWidth = (int) (screenWidth * IMAGE_WIDTH_PERCENT);
                int bitmapHeight = (int) (screenWidth * IMAGE_WIDTH_PERCENT);
                // fill data for load image
                DiagramImageProcessor.Data data = new DiagramImageProcessor.Data(item.diagramId, boardView);

                // load image out off UI thread
                diagramImageProcessor.setImageSize(bitmapWidth, bitmapHeight);
                diagramImageProcessor.loadImage(data, holder.imageView);

                // set text content
                loadTextWithImage(holder.contentTxt, item.textStr, mainArticleImageWidth);

                // add tags to handle clicks
                holder.contentTxt.setTag(itemListId, pos);
                holder.imageView.setTag(itemListId, pos);

                if (diagramToShow.getType() == ArticleDetailsItem.Diagram.SIMPLE) {
                    holder.iconView.setVisibility(View.GONE);
                    holder.userToMoveTxt.setVisibility(View.VISIBLE);
                    holder.userToMoveTxt.setText(diagramToShow.getUserToMove());
                } else {
                    holder.iconView.setVisibility(View.VISIBLE);
                    holder.iconView.setTag(itemListId, pos);
                    holder.userToMoveTxt.setVisibility(View.GONE);
                }

                // set players and game info
                String players = diagramToShow.getPlayers();
                if (TextUtils.isEmpty(players)) {
                    holder.playersTxt.setVisibility(View.GONE);
                } else {
                    holder.playersTxt.setVisibility(View.VISIBLE);
                    holder.playersTxt.setText(players);
                }

                String gameInfo = diagramToShow.getGameInfo();
                if (TextUtils.isEmpty(gameInfo)) {
                    holder.infoTxt.setVisibility(View.GONE);
                } else {
                    holder.infoTxt.setVisibility(View.VISIBLE);
                    holder.infoTxt.setText(gameInfo);
                }
            } else {
                ContentViewHolder holder = (ContentViewHolder) convertView.getTag();
                String contentPart = item.textStr;
                if (contentPart.contains("chess_com_diagram")) {
                    String diagramPart = contentPart.substring(contentPart.indexOf("<div "));
                    contentPart = diagramPart.substring(diagramPart.indexOf("</div>") + "</div>".length());
                }

                loadTextWithImage(holder.contentTxt, contentPart, mainArticleImageWidth);
            }
        }

        private class DiagramViewHolder {
            View fragmentContainer;
            ImageView imageView;
            TextView iconView;
            TextView contentTxt;
            TextView playersTxt;
            TextView infoTxt;
            TextView userToMoveTxt;
        }

        private class ContentViewHolder {
            TextView contentTxt;
        }
    }

    private void updateComments() {
        LoadItem loadItem = new LoadItem();
        loadItem.setLoadPath(RestHelper.getInstance().CMD_ARTICLE_COMMENTS(articleId));

        new RequestJsonTask<CommonCommentItem>(commentsUpdateListener).executeTask(loadItem);
    }

    private class CommentsUpdateListener extends ChessUpdateListener<CommonCommentItem> {

        private CommentsUpdateListener() {
            super(CommonCommentItem.class);
        }

        @Override
        public void showProgress(boolean show) {
            showCommentsLoadingView(show);
        }

        @Override
        public void updateData(CommonCommentItem returnedObj) {
            super.updateData(returnedObj);

            DbDataManager.updateArticleCommentToDb(getContentResolver(), returnedObj, articleId);

            Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getArticlesCommentsById(articleId));
            if (cursor != null && cursor.moveToFirst()) {
                commentsCursorAdapter.changeCursor(cursor);
            }

            // mark article as read
            handler.postDelayed(markAsReadRunnable, READ_DELAY);
        }
    }

    private void createPost() {
        createPost(NON_EXIST);
    }

    private void createPost(long commentId) {

        String body = getTextFromField(newPostEdt);
        if (TextUtils.isEmpty(body)) {
            newPostEdt.requestFocus();
            newPostEdt.setError(getString(R.string.can_not_be_empty));
            return;
        }

        LoadItem loadItem = new LoadItem();
        if (commentId == NON_EXIST) {
            loadItem.setLoadPath(RestHelper.getInstance().CMD_ARTICLE_COMMENTS(articleId));
            loadItem.setRequestMethod(RestHelper.POST);
        } else {
            loadItem.setLoadPath(RestHelper.getInstance().CMD_ARTICLE_EDIT_COMMENT(articleId, commentId));
            loadItem.setRequestMethod(RestHelper.PUT);
        }
        loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
        loadItem.addRequestParams(RestHelper.P_COMMENT_BODY, P_TAG_OPEN + body + P_TAG_CLOSE);

        new RequestJsonTask<PostCommentItem>(commentPostListener).executeTask(loadItem);
    }

    private class CommentPostListener extends ChessLoadUpdateListener<PostCommentItem> {

        private CommentPostListener() {
            super(PostCommentItem.class);
        }

        @Override
        public void updateData(PostCommentItem returnedObj) {
            if (returnedObj.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
                showToast(R.string.posted);
            } else {
                showToast(R.string.error);
            }
            showEditView(false);

            updateComments();
        }
    }

    private void showCommentsLoadingView(boolean show) {
        loadingCommentsView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public Context getMeContext() {
        return getActivity();
    }

    private void init() {
        Resources resources = getResources();
        authorImageSize = (int) (40 * density);
        contentPartsList = new ArrayList<DiagramListItem>();

        // little hack here
        // we know the with of left visible menu for portrait!
        if (inLandscape()) {
            screenWidth -= getResources().getDimensionPixelSize(R.dimen.tablet_side_menu_width) * 2;
        }

        {// set imageCache params for diagramProcessor
            ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

            cacheParams.setMemCacheSizePercent(0.15f); // Set memory cache to 25% of app memory

            diagramImageProcessor = new DiagramImageProcessor(getActivity(), DiagramImageProcessor.DEFAULT);
            diagramImageProcessor.setLoadingImage(R.drawable.board_green_default);
            diagramImageProcessor.setNeedLoadingImage(false);
            diagramImageProcessor.setChangingDrawable(resources.getDrawable(R.drawable.board_green_default));
            diagramImageProcessor.addImageCache(getFragmentManager(), cacheParams);
        }

        {// set imageCache params for articleImageFetcher
            ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getActivity(), THUMBS_CACHE_DIR);

            cacheParams.setMemCacheSizePercent(0.15f); // Set memory cache to 25% of app memory
            articleImageFetcher = new SmartImageFetcher(getActivity());
            articleImageFetcher.setLoadingImage(R.drawable.img_profile_picture_stub);
            articleImageFetcher.addImageCache(getFragmentManager(), cacheParams);
        }

        simpleIdsMap = new SparseBooleanArray();

        textColor = resources.getColor(R.color.new_subtitle_dark_grey);
        textSize = (int) (resources.getDimensionPixelSize(R.dimen.content_text_size) / density);
        infoTextSize = (int) (resources.getDimensionPixelSize(R.dimen.content_info_text_size) / density);
        paddingSide = resources.getDimensionPixelSize(R.dimen.default_scr_side_padding);
        iconOverlaySize = (int) (resources.getDimension(R.dimen.diagram_icon_overlay_size) / density);
        iconOverlayColor = resources.getColor(R.color.semitransparent_white_75);

        // for tablets make diagram wider
        if (isTablet) {
            IMAGE_WIDTH_PERCENT = 0.85f;
        }

        String[] countryNames = resources.getStringArray(R.array.new_countries);
        int[] countryCodes = resources.getIntArray(R.array.new_country_ids);
        countryMap = new SparseArray<String>();
        for (int i = 0; i < countryNames.length; i++) {
            countryMap.put(countryCodes[i], countryNames[i]);
        }

        articleUpdateListener = new ArticleUpdateListener();
        commentsUpdateListener = new CommentsUpdateListener();
        commentsCursorAdapter = new CommentsCursorAdapter(this, null, getImageFetcher());

        commentPostListener = new CommentPostListener();

        sectionedAdapter = new CustomSectionedAdapter(this, R.layout.arrow_section_header,
                new int[]{CONTENT_SECTION, COMMENTS_SECTION});

        diagramsAdapter = new DiagramsAdapter(getActivity(), null);
        sectionedAdapter.addSection(getString(R.string.content), diagramsAdapter);
        sectionedAdapter.addSection(getString(R.string.comments), commentsCursorAdapter);
    }

    private void widgetsInit(View view) {
        ViewGroup headerView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.article_details_header_frame, null, false);

        loadingView = view.findViewById(R.id.loadingView);
        loadingCommentsView = headerView.findViewById(R.id.loadingCommentsView);

        titleTxt = (TextView) headerView.findViewById(R.id.titleTxt);
        articleImg = (ProgressImageView) headerView.findViewById(R.id.articleImg);
        authorImg = (ProgressImageView) headerView.findViewById(R.id.thumbnailAuthorImg);
        countryImg = (ImageView) headerView.findViewById(R.id.countryImg);
        dateTxt = (TextView) headerView.findViewById(R.id.dateTxt);
        contentTxt = (TextView) headerView.findViewById(R.id.contentTxt);
        contentTxt.setMovementMethod(LinkMovementMethod.getInstance());
        authorTxt = (TextView) headerView.findViewById(R.id.authorTxt);

        ControlledListView listView = (ControlledListView) view.findViewById(R.id.listView);
        listView.addHeaderView(headerView);
        listView.setAdapter(sectionedAdapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    diagramImageProcessor.setPauseWork(true);
                } else {
                    diagramImageProcessor.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
        replyView = view.findViewById(R.id.replyView);
        newPostEdt = (EditText) view.findViewById(R.id.newPostEdt);

        initUpgradeAndAdWidgets(view);

        if (!needToShowAds()) {
            // we need to bind to bottom if there is no ad banner
            ((RelativeLayout.LayoutParams) replyView.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
    }

}
