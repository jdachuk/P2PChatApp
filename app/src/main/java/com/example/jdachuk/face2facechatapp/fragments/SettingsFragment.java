package com.example.jdachuk.face2facechatapp.fragments;

/**
 * Created by jdachuk on 17.02.18.
 */

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jdachuk.face2facechatapp.MainActivity;
import com.example.jdachuk.face2facechatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment
    implements Toolbar.OnMenuItemClickListener{

    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private StorageReference mImageStorage;

    private CircleImageView mUserPhoto;
    private TextView mUserName, mUserStatus;
    private Uri mCameraImageUri;
    private String completePath;
    private String fileName;

    private final static int GALLERY_PICK_IMAGE = 1;
    private final static int CAMERA_PICK_IMAGE = 2;
    private final static int PERMISSION_REQUEST = 27;

    private final static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    public SettingsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        if(getActivity() != null) {
            ((MainActivity) getActivity()).checkAuth();
        }
        mUserDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(mCurrentUser.getUid());
        mUserDatabase.keepSynced(true);
        mImageStorage = FirebaseStorage.getInstance().getReference();

        Toolbar toolbar = view.findViewById(R.id.settings_toolbar);
        toolbar.inflateMenu(R.menu.settings_toolbar_menu);
        toolbar.setOnMenuItemClickListener(this);

        mUserPhoto = view.findViewById(R.id.user_photo);
        mUserName = view.findViewById(R.id.user_name);
        mUserStatus = view.findViewById(R.id.user_status);

        loadData();

        view.findViewById(R.id.change_status_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkPermissions();

                CharSequence[] options = new CharSequence[]
                        {"Choose from Gallery.", "Take new Photo."};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File folder = new File(Environment.getExternalStorageDirectory()
                                .getPath() + "/PToPchat/Images");
                        folder.mkdirs();
                        completePath = folder.toString();
                        fileName = "profile_image.jpg";

                        switch (which){
                            case 0: {
                                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                                startActivityForResult(galleryIntent, GALLERY_PICK_IMAGE);
                                break;
                            }
                            case 1: {
                                File file = new File(completePath, fileName);
                                mCameraImageUri = Uri.fromFile(file);

                                Intent cameraIntent =
                                        new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraImageUri);
                                startActivityForResult(cameraIntent, CAMERA_PICK_IMAGE);

                                break;
                            }
                        }
                    }
                });
                builder.show();
            }
        });
    }

    private void checkPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(getActivity() != null) {
                for (String permission : PERMISSIONS) {
                    if (ContextCompat.checkSelfPermission(getActivity(), permission) !=
                            PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(PERMISSIONS, PERMISSION_REQUEST);
                    }
                }
            }
        }
    }

    private void loadData() {
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue() + "";
                final String thumb_image = dataSnapshot.child("thumb_image").getValue() + "";
                String status = dataSnapshot.child("status").getValue() + "";

                mUserName.setText(name);
                mUserStatus.setText(status);
                if(!thumb_image.equals("default")) {
                    Picasso.with(getContext()).load(thumb_image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mUserPhoto, new Callback() {
                                @Override
                                public void onSuccess() {}

                                @Override
                                public void onError() {
                                    Picasso.with(getContext()).load(thumb_image)
                                            .into(mUserPhoto);
                                }
                            });
                } else {
                    final String image = dataSnapshot.child("image").getValue() + "";
                    if(!image.equals("default")) {
                        Picasso.with(getContext()).load(image)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .into(mUserPhoto, new Callback() {
                                    @Override
                                    public void onSuccess() {}

                                    @Override
                                    public void onError() {
                                        Picasso.with(getContext()).load(image)
                                                .into(mUserPhoto);
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.log_out) {
            Map<String, Object> logOutMap = new HashMap<>();
            logOutMap.put("online", false);
            logOutMap.put("last_seen", System.currentTimeMillis());
            logOutMap.put("device_token", null);

            mUserDatabase.updateChildren(logOutMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mAuth.signOut();
                            if(getActivity() != null) {
                                ((MainActivity) getActivity()).sendToLogIn();
                            }
                        }
                    });
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK_IMAGE) {

            if (resultCode == RESULT_OK) {

                File file = new File(completePath, fileName);
                Uri imageTargetUri = Uri.fromFile(file);

                Uri imageUri = data.getData();

                if (imageUri != null && getContext() != null) {
                    UCrop.of(imageUri, imageTargetUri)
                            .withAspectRatio(1, 1)
                            .start(getContext(), this);
                }

            }
        }

        if (requestCode == CAMERA_PICK_IMAGE) {

            if (resultCode == RESULT_OK) {

                File file = new File(completePath, fileName);
                Uri imageTargetUri = Uri.fromFile(file);

                if (getContext() != null) {
                    UCrop.of(mCameraImageUri, imageTargetUri)
                            .withAspectRatio(1, 1)
                            .start(getContext(), this);
                }
            }

        }

        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {

            Uri resultUri = UCrop.getOutput(data);

            mUserPhoto.setImageURI(resultUri);

            String current_uid = mCurrentUser.getUid();

            StorageReference filepath = mImageStorage.child("profile_photos")
                    .child(current_uid + ".jpg");

            if (resultUri != null) {
                filepath.putFile(resultUri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {

                                    String download_url = task.getResult().getDownloadUrl() + "";

                                    Map<String, Object> updateMap = new HashMap<>();
                                    updateMap.put("image", download_url);

                                    mUserDatabase.updateChildren(updateMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (!task.isSuccessful()) {
                                                        if (task.getException() != null)
                                                            Toast.makeText(getActivity(),
                                                                    task.getException().getLocalizedMessage(),
                                                                    Toast.LENGTH_SHORT).show();

                                                    }
                                                }
                                            });

                                } else {

                                    if (task.getException() != null)
                                        Toast.makeText(getContext(), task.getException()
                                                .getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }

        } else if (resultCode == UCrop.RESULT_ERROR) {

            final Throwable cropError = UCrop.getError(data);
            if (cropError != null)
                Toast.makeText(getActivity(), cropError.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }


}
