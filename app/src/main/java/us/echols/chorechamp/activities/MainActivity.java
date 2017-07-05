package us.echols.chorechamp.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import us.echols.chorechamp.Achievement;
import us.echols.chorechamp.Chore;
import us.echols.chorechamp.ChoreChampReceiver;
import us.echols.chorechamp.PasswordUtility;
import us.echols.chorechamp.R;
import us.echols.chorechamp.adapters.ChoreAdapter;
import us.echols.chorechamp.database.DbHelper;
import us.echols.chorechamp.dialogs.AddChildDialog;
import us.echols.chorechamp.dialogs.ConfirmDeleteDialog;
import us.echols.chorechamp.dialogs.ConfirmPasswordDialog;
import us.echols.chorechamp.fragments.AchievementListFragment;
import us.echols.chorechamp.fragments.ChoreListFragment;
import us.echols.chorechamp.fragments.HomeFragment;
import us.echols.chorechamp.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, AddChildDialog.AddChildDialogListener,
        ChoreListFragment.OnChoreListListener {

    private static final String TAG_DIALOG_ADD_CHILD = "us.echols.chorechamp.TAG.add_child_dialog";
    private static final String TAG_DIALOG_CONFIRM_PASSWORD = "us.echols.chorechamp.TAG.confirm_password_dialog";
    private static final String TAG_FRAGMENT_HOME_SCREEN = "us.echols.chorechamp.TAG.home_screen_fragment";
    private static final String TAG_FRAGMENT_CHORE_LIST = "us.echols.chorechamp.TAG.chore_list_fragment";
    private static final String TAG_FRAGMENT_ACHIEVEMENT_LIST = "us.echols.chorechamp.TAG.achievement_list_fragment";
    private static final String TAG_FRAGMENT_SETTINGS = "us.echols.chorechamp.TAG.settings_fragment";
    private static final String TAG_ACTIVE_FRAGMENT = "active_fragment";

    public static final String EXTRA_CHILD_NAME = "us.echols.chorechamp.EXTRA.child_name";
    public static final String EXTRA_CHORE_ID = "us.echols.chorechamp.EXTRA.chore_id";

    private enum RequestType {
        ADD_CHORE(0), EDIT_CHORE(1);

        final int value;

        RequestType(int value) {
            this.value = value;
        }

        int getValue() {
            return value;
        }

        @Nullable
        static RequestType getEnum(int value) {
            for (RequestType r : RequestType.values()) {
                if (r.getValue() == value) {
                    return r;
                }
            }
            return null;
        }
    }

    private enum FragmentType {
        HOME, CHORES_CHILD, CHORES_PARENT, SETTINGS, ACHIEVEMENTS
    }

    private FragmentType fragmentType;

    private Toolbar toolbar;
    private FloatingActionButton fab;
    private NavigationView navigationView;
    private FragmentManager fragmentManager;

    private DbHelper dbHelper; // the database helper object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize the database helper
        dbHelper = DbHelper.getInstance(this);
        dbHelper.addAchievements();

        // get toolbar properties
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // get floating action button properties
        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addChild();
            }
        });

        // set drawer properties
        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        initializeNavigationView();

        // add a back stack listener to the fragment manager which will update the fragment
        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                updateFragment();
            }
        });

        // add the home fragment
        Fragment fragment = new HomeFragment();
        fragmentType = FragmentType.HOME;
        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment, TAG_FRAGMENT_HOME_SCREEN).commit();

        // get the encryption utility
        PasswordUtility passwordUtility = PasswordUtility.getInstance(this);

        // check if there is a password for the parent
        boolean passwordExists = passwordUtility.checkForPasswordFile();
        if (!passwordExists) {
            Intent intent = new Intent(this, SetPasswordActivity.class);
            startActivity(intent);
        }

        ChoreChampReceiver receiver = new ChoreChampReceiver();
        receiver.setAlarm(this);
//        scheduleAlarm();
    }

    private void initializeNavigationView() {
        navigationView = (NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        String[] childNames = dbHelper.getChildNames();
        for (String name : childNames) {
            addChildToNavView(name);
        }
    }

    private void addChildToNavView(String name) {
        Menu navMenu = navigationView.getMenu();
        long id = dbHelper.getChildId(name);
        try {
            navMenu.getItem((int)id);
        } catch (IndexOutOfBoundsException e) {
            navMenu.add(R.id.nav_child_list, (int)id, (int)id, name).setIcon(R.drawable.ic_person);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            recheckForPassword();
        }
    }

    private void recheckForPassword() {
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment instanceof SettingsFragment) {
            PasswordUtility.confirmPassword(fragmentManager, "some tag", new ConfirmPasswordDialog.ConfirmPasswordDialogListener() {
                @Override
                public void onConfirmPassword() {

                }

                @Override
                public void onBadPassword() {
                    fragmentManager.popBackStack();
                }

                @Override
                public void onCancel() {
                    fragmentManager.popBackStack();
                }
            });
        } else if (fragment instanceof ChoreListFragment) {
            String childName = ((ChoreListFragment)fragment).getChildName();
            if (childName.equals(getString(R.string.parent_name))) {
                PasswordUtility.confirmPassword(fragmentManager, "some tag", new ConfirmPasswordDialog.ConfirmPasswordDialogListener() {
                    @Override
                    public void onConfirmPassword() {

                    }

                    @Override
                    public void onBadPassword() {
                        fragmentManager.popBackStack();
                    }

                    @Override
                    public void onCancel() {
                        fragmentManager.popBackStack();
                    }
                });
            }
        }
    }

    private void updateFragment() {
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

        String title = getString(R.string.app_name);

        if (fragment instanceof ChoreListFragment) {
            title = ((ChoreListFragment)fragment).getChildName();
            if (title.equals(getString(R.string.parent_name))) {
                title = getString(R.string.title_chores_pending);
                fragmentType = FragmentType.CHORES_PARENT;
            } else {
                fragmentType = FragmentType.CHORES_CHILD;
            }
            modifyFAB(true, R.drawable.ic_add);
        } else if (fragment instanceof SettingsFragment) {
            title = getString(R.string.title_settings);
            fragmentType = FragmentType.SETTINGS;
            modifyFAB(false, 0);
        } else if (fragment instanceof HomeFragment) {
            title = getString(R.string.app_name);
            fragmentType = FragmentType.HOME;
            modifyFAB(true, R.drawable.ic_person_add);
        } else if (fragment instanceof AchievementListFragment) {
            title = getString(R.string.title_achievements);
            fragmentType = FragmentType.ACHIEVEMENTS;
            modifyFAB(false, 0);
        }

        toolbar.setTitle(title);
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater menuInflater = getMenuInflater();
        switch (fragmentType) {
            case HOME:
                menuInflater.inflate(R.menu.menu_main, menu);
                break;
            case CHORES_CHILD:
                menuInflater.inflate(R.menu.menu_main, menu);
                break;
            case ACHIEVEMENTS:
                menuInflater.inflate(R.menu.menu_main, menu);
                MenuItem achievementMenuItem = menu.findItem(R.id.action_achievements);
                achievementMenuItem.setVisible(false);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_achievements) {
            Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
            String childName = null;
            if (fragment instanceof ChoreListFragment) {
                childName = ((ChoreListFragment)fragment).getChildName();
            }
            Fragment achievementListFragment = AchievementListFragment.newInstance(childName);
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, achievementListFragment, TAG_FRAGMENT_ACHIEVEMENT_LIST).addToBackStack(null).commit();
            updateFragment();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_approve_chores:
                confirmPasswordForApproveChores();
                break;
            case R.id.nav_delete_child:
                confirmPasswordForDeleteChild();
                break;
            case R.id.nav_parent_settings:
                confirmPasswordForParentSettings();
                break;
            default:
                String name = dbHelper.getChildName(id);
                if (name != null && !name.isEmpty()) {
                    Fragment fragment = ChoreListFragment.newInstance(name);
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment, TAG_FRAGMENT_CHORE_LIST).addToBackStack(null).commit();
                    updateFragment();
                }
                break;
        }

        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void modifyFAB(boolean show, int icon) {
        if (show) {
            fab.show();
            fab.setImageResource(icon);
            if (icon == R.drawable.ic_add) {
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addChore();
                    }
                });
            } else if (icon == R.drawable.ic_person_add) {
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addChild();
                    }
                });
            }
        } else {
            fab.hide();
        }
    }

    private void confirmPasswordForApproveChores() {
        PasswordUtility.confirmPassword(fragmentManager, TAG_DIALOG_CONFIRM_PASSWORD,
                new ConfirmPasswordDialog.ConfirmPasswordDialogListener() {
                    @Override
                    public void onConfirmPassword() {
                        Fragment fragment = ChoreListFragment.newInstance(getString(R.string.parent_name));
                        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment, TAG_FRAGMENT_CHORE_LIST).addToBackStack(null).commit();
                        updateFragment();
                    }

                    @Override
                    public void onBadPassword() {
                        showIncorrectPasswordToast();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    private void confirmPasswordForParentSettings() {
        PasswordUtility.confirmPassword(fragmentManager, TAG_DIALOG_CONFIRM_PASSWORD,
                new ConfirmPasswordDialog.ConfirmPasswordDialogListener() {
                    @Override
                    public void onConfirmPassword() {
                        Fragment fragment = new SettingsFragment();
                        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment, TAG_FRAGMENT_SETTINGS).addToBackStack(null).commit();
                        updateFragment();
                    }

                    @Override
                    public void onBadPassword() {
                        showIncorrectPasswordToast();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    private void confirmPasswordForDeleteChild() {
        final String[] childNames = dbHelper.getChildNames();
        if (childNames.length == 0) {
            Toast.makeText(this, getString(R.string.child_none_found), Toast.LENGTH_SHORT).show();
        } else {
            PasswordUtility.confirmPassword(fragmentManager, TAG_DIALOG_CONFIRM_PASSWORD,
                    new ConfirmPasswordDialog.ConfirmPasswordDialogListener() {
                        @Override
                        public void onConfirmPassword() {
                            getChildToDelete(childNames);
                        }

                        @Override
                        public void onBadPassword() {
                            showIncorrectPasswordToast();
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
        }
    }

    private void getChildToDelete(final String[] childNames) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog_Alert);

        alertDialogBuilder.setTitle("Select which child to delete...");

        alertDialogBuilder.setItems(childNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmDeleteChild(childNames[which]);
                dialog.dismiss();
            }
        });
        alertDialogBuilder.show();
    }

    private void confirmDeleteChild(final String childName) {
        String title = "Remove " + childName + " from app?";
        String message = "Are you sure you want to delete " + childName + "?\n" +
                "This will also delete all chores and achievements.";
        ConfirmDeleteDialog yesNoDialog = ConfirmDeleteDialog.newInstance(title, message);
        yesNoDialog.setConfirmDeleteListener(new ConfirmDeleteDialog.ConfirmDeleteListener() {
            @Override
            public void onConfirmDelete() {
                deleteChild(childName);
            }
        });
        yesNoDialog.show(getFragmentManager(), null);
    }

    private void deleteChild(String childName) {
        long id = dbHelper.getChildId(childName);
        dbHelper.deleteChild(id);
        Menu navMenu = navigationView.getMenu();

        try {
            navMenu.removeItem((int)id);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void showIncorrectPasswordToast() {
        Toast.makeText(this, getString(R.string.error_incorrect_password), Toast.LENGTH_LONG).show();
    }

    private void addChild() {
        AddChildDialog addChildDialog = AddChildDialog.newInstance();
        addChildDialog.show(fragmentManager, TAG_DIALOG_ADD_CHILD);
    }

    @Override
    public void onAddNewChild(String childName) {
        if (childName != null && !childName.isEmpty() && !childName.equals(getString(R.string.parent_name))) {
            dbHelper.addChild(childName);
            addChildToNavView(childName);
        }
    }

    private String getChildName() {
        String childName = null;

        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment instanceof ChoreListFragment) {
            childName = ((ChoreListFragment)fragment).getChildName();
        }

        return childName;
    }

    private void addChore() {
        Intent intent = new Intent(this, ChoreDetailsActivity.class);
        intent.putExtra(EXTRA_CHILD_NAME, getChildName());
        startActivityForResult(intent, RequestType.ADD_CHORE.getValue());
    }

    @Override
    public void onChoreClick(int position, Chore chore) {
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment instanceof ChoreListFragment) {
            ChoreAdapter adapter = ((ChoreListFragment)fragment).getAdapter();

            if (actionMode != null) {
                adapter.toggleSelection(position);
                if (adapter.getSelectedItemCount() == 0) {
                    actionMode.finish();
                }
            } else {
                Intent intent = new Intent(this, ChoreDetailsActivity.class);
                intent.putExtra(EXTRA_CHILD_NAME, chore.getChildName());
                intent.putExtra(EXTRA_CHORE_ID, chore.getId());

                startActivityForResult(intent, RequestType.EDIT_CHORE.getValue());
            }
        }
    }

    @Override
    public void onChoreLongClick(int position) {
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment instanceof ChoreListFragment) {
            ChoreAdapter adapter = ((ChoreListFragment)fragment).getAdapter();
            adapter.toggleSelection(position);

            if (actionMode == null) {
                actionMode = startActionMode(modeCallBack);
            } else if (adapter.getSelectedItemCount() == 0) {
                actionMode.finish();
            }
        }
    }

    private ActionMode actionMode;

    private final ActionMode.Callback modeCallBack = new ActionMode.Callback() {
        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);
            fab.hide();
            Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
            if (fragment instanceof ChoreListFragment) {
                String childName = ((ChoreListFragment)fragment).getChildName();
                if (!childName.equals(getString(R.string.parent_name))) {
                    menu.removeItem(R.id.action_delete_chores);
                }
            }
            return true;
        }

        // Called each time the action mode is shown.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_approve_chores:
                    approveChores();
                    mode.finish();
                    return true;
                case R.id.action_delete_chores:
                    deleteChores();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            fab.show();
            Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
            if (fragment instanceof ChoreListFragment) {
                ChoreAdapter adapter = ((ChoreListFragment)fragment).getAdapter();
                adapter.clearSelections();
            }
        }
    };

    private void approveChores() {
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment instanceof ChoreListFragment) {
            ChoreAdapter adapter = ((ChoreListFragment)fragment).getAdapter();

            // get the list of selected indexes
            List<Integer> selectedIndexes = adapter.getSelectedIndexes();

            // sort the list in reverse order
            Collections.sort(selectedIndexes, new Comparator<Integer>() {
                @Override
                public int compare(Integer lhs, Integer rhs) {
                    return rhs - lhs;
                }
            });

            // get all the chores in the list
            List<Chore> chores = adapter.getChores();

            // get the list of chores to update
            List<Chore> choresToUpdate = new ArrayList<>();
            for (int i = 0; i < selectedIndexes.size(); i++) {
                choresToUpdate.add(chores.get(selectedIndexes.get(i)));
            }

            // update the chores in the database
            for (Chore chore : choresToUpdate) {
                // increase approve status of chore object
                chore.approve();
                // update the chore in the database
                dbHelper.updateChore(chore);
                // update the child's chore counts for achievements
                String parentName = ((ChoreListFragment)fragment).getChildName();
                if (parentName.equals(getString(R.string.parent_name))) {
                    String childName = chore.getChildName();
                    dbHelper.updateChildChoreCount(childName);

                    // check for achievement completion
                    Achievement achievementJustCompleted = dbHelper.checkForAchievements(childName);
                    if (achievementJustCompleted != null) {
                        String achievementName = achievementJustCompleted.getName();
                        long childId = dbHelper.getChildId(childName);
                        showAchievementNotification(achievementName, achievementJustCompleted.getCount() + (int)childId);
                    }
                }
            }

            // remove the chores from the list structure
            chores.removeAll(choresToUpdate);

            // update the adapter
            removeChoresFromListAdapter(adapter, selectedIndexes);

        }
    }

    private void deleteChores() {
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment instanceof ChoreListFragment) {
            ChoreAdapter adapter = ((ChoreListFragment)fragment).getAdapter();

            // get the list of selected indexes
            List<Integer> selectedIndexes = adapter.getSelectedIndexes();

            // sort the list in reverse order
            Collections.sort(selectedIndexes, new Comparator<Integer>() {
                @Override
                public int compare(Integer lhs, Integer rhs) {
                    return rhs - lhs;
                }
            });

            // get all the chores in the list
            List<Chore> chores = adapter.getChores();

            // get the list of chores to delete
            List<Chore> choresToDelete = new ArrayList<>();
            for (int i = 0; i < selectedIndexes.size(); i++) {
                choresToDelete.add(chores.get(selectedIndexes.get(i)));
            }

            // remove the chores from the database
            dbHelper.deleteChores(choresToDelete);

            // remove the chores from the list structure
            chores.removeAll(choresToDelete);

            // update the adapter
            removeChoresFromListAdapter(adapter, selectedIndexes);
        }
    }

    private void removeChoresFromListAdapter(ChoreAdapter adapter, List<Integer> selectedIndexes) {
        // continue to notify the adapter of removals until all indexes have been removed
        while (!selectedIndexes.isEmpty()) {

            // if there is only one item left, remove it
            if (selectedIndexes.size() == 1) {
                adapter.notifyItemRemoved(selectedIndexes.get(0));
                selectedIndexes.remove(0);
            } else {
                // count the number items that are in the next consecutive block
                int count = 1;
                while (selectedIndexes.size() > count && selectedIndexes.get(count).equals(selectedIndexes.get(count - 1) - 1)) {
                    ++count;
                }

                // notify the adapter that we have remove the next consecutive block
                if (count == 1) {
                    adapter.notifyItemRemoved(selectedIndexes.get(0));
                } else {
                    adapter.notifyItemRangeRemoved(selectedIndexes.get(count - 1), count);
                }

                // remove the next consecutive block
                for (int i = 0; i < count; ++i) {
                    selectedIndexes.remove(0);
                }
            }
        }
    }

    private void showAchievementNotification(String achievementName, int id) {
        // build the notification
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.achievement_notification))
                .setContentText(achievementName)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .build();

        // send the notification to the notification manager
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        RequestType request = RequestType.getEnum(requestCode);
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

        // return if any required data is null or the result is not OK
        if (request == null || resultCode != RESULT_OK) {
            return;
        }

        long id = data.getLongExtra(getString(R.string.intent_chore_id), 0);

        switch (request) {
            case ADD_CHORE:
                ((ChoreListFragment)fragment).addChore(dbHelper.getChoreById(id));
                break;
            case EDIT_CHORE:
                ((ChoreListFragment)fragment).updateChore(dbHelper.getChoreById(id));
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        fragmentManager.putFragment(outState, TAG_ACTIVE_FRAGMENT, fragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            Fragment fragment = fragmentManager.getFragment(savedInstanceState, TAG_ACTIVE_FRAGMENT);
            String tag = fragment.getTag();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment, tag).commit();
        }
    }
}
