package it.alessioricco.tsflickr.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import it.alessioricco.tsflickr.R;
import it.alessioricco.tsflickr.adapters.GalleryAdapter;
import it.alessioricco.tsflickr.fragments.FullScreenDialogFragment;
import it.alessioricco.tsflickr.injection.ObjectGraphSingleton;
import it.alessioricco.tsflickr.models.FlickrFeed;
import it.alessioricco.tsflickr.models.FlickrFeedItem;
import it.alessioricco.tsflickr.models.GalleryImage;
import it.alessioricco.tsflickr.models.GalleryImages;
import it.alessioricco.tsflickr.services.FlickrService;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected CompositeSubscription compositeSubscription = new CompositeSubscription();
    @Inject
    FlickrService flickrService;

    private final String TAG = MainActivity.class.getSimpleName();

    private final GalleryImages images = new GalleryImages();
    private ProgressDialog pDialog;
    private GalleryAdapter galleryAdapter;

    @InjectView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //custom code
        initialize();

    }

    private void initialize() {

        final Context context = getApplicationContext();

        ObjectGraphSingleton.getInstance().inject(this);
        ButterKnife.inject(this);

        pDialog = new ProgressDialog(this);
        galleryAdapter = new GalleryAdapter(getApplicationContext(), images);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(galleryAdapter);
        recyclerView.addOnItemTouchListener(new GalleryAdapter.RecyclerTouchListener(context,
                recyclerView,
                new GalleryAdapter.ClickListener() {

            @Override
            public void onClick(final View view, final int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(getString(R.string.param_images), (Serializable) images);
                bundle.putInt(getString(R.string.param_position), position);

                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                final FullScreenDialogFragment newFragment = FullScreenDialogFragment.create();
                newFragment.setArguments(bundle);
                newFragment.show(ft, "slideshow");
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * https://github.com/ReactiveX/RxJava/wiki/The-RxJava-Android-Module
     */
    @Override
    public void onResume() {
        super.onResume();

        compositeSubscription.add(asyncFetchImages());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // avoid memory leaks
        compositeSubscription.unsubscribe();
    }

    void startProgress() {
        if (pDialog == null) {
            return;
        }
        pDialog.setMessage(getString(R.string.downloading));
        pDialog.show();
    }

    void endProgress() {
        if (pDialog == null) {
            return;
        }
        pDialog.hide();
    }

    private Subscription asyncFetchImages() {

        startProgress();
        final Observable<FlickrFeed> observable = flickrService.getPublicFeed();

        return observable
                .subscribeOn(Schedulers.io()) // optional if you do not wish to override the default behavior
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<FlickrFeed>() {
                    @Override
                    public void onCompleted() {
                        endProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        // cast to retrofit.HttpException to get the response code
                        if (e instanceof HttpException) {
                            HttpException response = (HttpException) e;
                            int code = response.code();
                            //TODO: add a toast
                        }
                        //TODO: what happens to the UI?
                        endProgress();
                    }

                    @Override
                    public void onNext(FlickrFeed feed) {

                        //todo: apply a map transformation
                        images.clear();
                        if (feed.getItems() == null) {
                            // this should never happen
                            return;
                        }
                        for (FlickrFeedItem item: feed.getItems()) {
                            if (!GalleryImage.isValid(item)) {
                                continue;
                            }
                            images.add(new GalleryImage(item));
                        }

                        galleryAdapter.notifyDataSetChanged();
                    }
                });
    }
}
