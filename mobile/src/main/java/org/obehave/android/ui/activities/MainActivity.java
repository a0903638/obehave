package org.obehave.android.ui.activities;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.google.common.eventbus.Subscribe;
import org.obehave.android.R;
import org.obehave.android.application.MyApplication;
import org.obehave.android.events.NodeSelectedEvent;
import org.obehave.android.ui.adapters.SectionsPagerAdapter;
import org.obehave.android.ui.events.*;
import org.obehave.android.ui.exceptions.UiException;
import org.obehave.android.ui.fragments.*;
import org.obehave.android.util.ErrorDialog;
import org.obehave.events.EventBusHolder;
import org.obehave.exceptions.FactoryException;
import org.obehave.model.Action;
import org.obehave.model.Subject;
import org.obehave.model.modifier.ModifierFactory;

import java.util.List;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    private static final int CODING_FRAGMENT_POSITION = 2;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Subscribe
    public void onSubjectSelected(SubjectSelectedEvent event) {
        mSectionsPagerAdapter.switchToNextCodingFragment(ActionFragment.newInstance(CODING_FRAGMENT_POSITION));
    }

    @Subscribe
    public void onActionSelected(ActionSelectedEvent event) {
        Action action = event.getAction();
        Log.d(LOG_TAG, "onActionSelected");
        Log.d(LOG_TAG, action.getDisplayString());
        try {
            MyApplication.selectItem(action);
            ModifierFactory modifierFactory = MyApplication.getModifierFactoryOfSelectedAction();
            if (modifierFactory == null) {
                MyApplication.createCoding();
            } else if (modifierFactory.getType() == ModifierFactory.Type.SUBJECT_MODIFIER_FACTORY) {
                replaceFragment(SubjectModifierFragment.newInstance(CODING_FRAGMENT_POSITION, (modifierFactory).getValidSubjects()));
            } else if (modifierFactory.getType() == ModifierFactory.Type.ENUMERATION_MODIFIER_FACTORY) {
                replaceFragment(EnumerationModifierFragment.newInstance(CODING_FRAGMENT_POSITION, (modifierFactory).getValidValues()));
            } else if (modifierFactory.getType() == ModifierFactory.Type.DECIMAL_RANGE_MODIFIER_FACTORY) {
                replaceFragment(DecimalRangeModifierFragment.newInstance(CODING_FRAGMENT_POSITION, modifierFactory.getFrom(), modifierFactory.getTo()));
            }
        } catch (UiException ex) {
            ErrorDialog ed = new ErrorDialog(ex.getMessage(), this);
            ed.invoke();
        }
    }

    @Subscribe
    public void onSubjectModifierSelected(SubjectModifierSelectedEvent event) {
        List<Subject> subjects = event.getSubjects();
        try {
            if (subjects.isEmpty()) {
                throw new UiException("Es muss mindestens ein Subjekt gewählt werden.");
            }

            ModifierFactory subjectModifierFactory = (ModifierFactory) MyApplication.getSelectedAction().getModifierFactory();
            MyApplication.selectItem(subjectModifierFactory.create(subjects.get(0).getName()));
            MyApplication.createCoding();
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            replaceFragment(SubjectFragment.newInstance(CODING_FRAGMENT_POSITION));

        } catch (UiException exception) {
            ErrorDialog ed = new ErrorDialog(exception, this);
            ed.invoke();
        } catch (FactoryException e) {
            ErrorDialog ed = new ErrorDialog(e.getMessage(), this);
            ed.invoke();
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onEnumerationModifierSelected(EnumerationModifierSelectedEvent event) {
        List<String> values = event.getValues();
        try {
            if (values.isEmpty()) {
                throw new UiException("Es muss mindestens ein Wert gewählt werden.");
            }

            ModifierFactory enumerationModifierFactory = MyApplication.getSelectedAction().getModifierFactory();
            MyApplication.selectItem(enumerationModifierFactory.create(values.get(0)));
            MyApplication.createCoding();
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            replaceFragment(SubjectFragment.newInstance(CODING_FRAGMENT_POSITION));
        } catch (UiException exception) {
            ErrorDialog ed = new ErrorDialog(exception, this);
            ed.invoke();
        } catch (FactoryException e) {
            ErrorDialog ed = new ErrorDialog(e.getMessage(), this);
            ed.invoke();
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onTimerStartEvent(TimerStartEvent event) {
        Log.d(LOG_TAG, "onTimerStartEvent");
        MyApplication.startTimer();
    }

    @Subscribe
    public void onTimerStopEvent(TimerStopEvent event) {
        Log.d(LOG_TAG, "onTimerStopEvent");
        MyApplication.stopTimer();
    }

    @Subscribe
    public void onDecimalRangeModifierSelected(DecimalRangeModifierSelectedEvent event) {
        String value = event.getValue();
        try {
            ModifierFactory modifierFactory = MyApplication.getSelectedAction().getModifierFactory();
            MyApplication.selectItem(modifierFactory.create(value));
            MyApplication.createCoding();
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            replaceFragment(SubjectFragment.newInstance(CODING_FRAGMENT_POSITION));

        } catch (FactoryException e) {
            ErrorDialog ed = new ErrorDialog(e.getMessage(), this);
            ed.invoke();
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onNodeSelected(NodeSelectedEvent event) {
        if (event.getNodeType() == NodeSelectedEvent.NodeType.SUBJECT) {
            mSectionsPagerAdapter.switchToNextCodingFragment(SubjectFragment.newInstance(CODING_FRAGMENT_POSITION, event.getNode()));
        } else if (event.getNodeType() == NodeSelectedEvent.NodeType.ACTION) {
            mSectionsPagerAdapter.switchToNextCodingFragment(ActionFragment.newInstance(CODING_FRAGMENT_POSITION, event.getNode()));
        }
    }

    private void replaceFragment(Fragment fragment) {
        Log.d(LOG_TAG, "Fragment - Change Coding Fragment");
        mSectionsPagerAdapter.switchToNextCodingFragment(fragment);
    }

    @Override
    public void onBackPressed() {
        if(getActionBar().getSelectedTab().getPosition() == CODING_FRAGMENT_POSITION){
            if(!mSectionsPagerAdapter.back()){
                finish();
            }
        }
        else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if(savedInstanceState == null

        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(actionBar, mViewPager, getSupportFragmentManager(), SubjectFragment.newInstance(CODING_FRAGMENT_POSITION));
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            String title = "";
            Integer resourceTitle = mSectionsPagerAdapter.getPageTitleResource(i);
            if (resourceTitle != null) {
                title = getString(resourceTitle);
            }

            actionBar.addTab(
                    actionBar.newTab()
                            .setText(title)
                            .setTabListener(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_open_coding_schema, menu);
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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /* TODO: Check if timer is stopped  */
        MyApplication.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
        EventBusHolder.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
        EventBusHolder.unregister(this);
    }

}
