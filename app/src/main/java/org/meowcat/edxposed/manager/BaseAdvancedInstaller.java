package org.meowcat.edxposed.manager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;

import org.meowcat.edxposed.manager.util.NavUtil;
import org.meowcat.edxposed.manager.util.json.XposedTab;
import org.meowcat.edxposed.manager.util.json.XposedZip;

import java.util.List;
import java.util.Objects;

import static org.meowcat.edxposed.manager.XposedApp.WRITE_EXTERNAL_PERMISSION;

public class BaseAdvancedInstaller extends Fragment {

    private View mClickedButton;

    static BaseAdvancedInstaller newInstance(XposedTab tab) {
        BaseAdvancedInstaller myFragment = new BaseAdvancedInstaller();

        Bundle args = new Bundle();
        args.putParcelable("tab", tab);
        myFragment.setArguments(args);

        return myFragment;
    }

    private List<XposedZip> installers() {
        XposedTab tab = requireArguments().getParcelable("tab");
        return Objects.requireNonNull(tab).installers;
    }

    private List<XposedZip> uninstallers() {
        XposedTab tab = requireArguments().getParcelable("tab");
        return Objects.requireNonNull(tab).uninstallers;
    }

    private String notice() {
        XposedTab tab = requireArguments().getParcelable("tab");
        return Objects.requireNonNull(tab).notice;
    }

    protected String author() {
        XposedTab tab = requireArguments().getParcelable("tab");
        return Objects.requireNonNull(tab).author;
    }

    private String supportUrl() {
        XposedTab tab = requireArguments().getParcelable("tab");
        return Objects.requireNonNull(tab).support;
    }

    private boolean isStable() {
        XposedTab tab = requireArguments().getParcelable("tab");
        return Objects.requireNonNull(tab).stable;
    }

    private boolean isOfficial() {
        XposedTab tab = requireArguments().getParcelable("tab");
        return Objects.requireNonNull(tab).official;
    }

    private String description() {
        XposedTab tab = requireArguments().getParcelable("tab");
        return Objects.requireNonNull(tab).description;
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_PERMISSION);
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.single_installer_view, container, false);

        final Spinner chooserInstallers = view.findViewById(R.id.chooserInstallers);
        final Spinner chooserUninstallers = view.findViewById(R.id.chooserUninstallers);
        final Button btnInstall = view.findViewById(R.id.btnInstall);
        final Button btnUninstall = view.findViewById(R.id.btnUninstall);
        ImageView infoInstaller = view.findViewById(R.id.infoInstaller);
        ImageView infoUninstaller = view.findViewById(R.id.infoUninstaller);
        TextView noticeTv = view.findViewById(R.id.noticeTv);
        TextView author = view.findViewById(R.id.author);
        View showOnXda = view.findViewById(R.id.show_on_xda);
        View updateDescription = view.findViewById(R.id.updateDescription);

        try {
            chooserInstallers.setAdapter(new XposedZip.MyAdapter(getContext(), installers()));
            chooserUninstallers.setAdapter(new XposedZip.MyAdapter(getContext(), uninstallers()));
        } catch (Exception ignored) {
        }

        infoInstaller.setOnClickListener(v -> {
            XposedZip selectedInstaller = (XposedZip) chooserInstallers.getSelectedItem();
            String s = getString(R.string.infoInstaller,
                    selectedInstaller.name,
                    selectedInstaller.version);

            new MaterialDialog.Builder(requireContext()).title(R.string.info)
                    .content(s).positiveText(R.string.ok).show();
        });
        infoUninstaller.setOnClickListener(v -> {
            XposedZip selectedUninstaller = (XposedZip) chooserUninstallers.getSelectedItem();
            String s = getString(R.string.infoUninstaller,
                    selectedUninstaller.name,
                    selectedUninstaller.version);

            new MaterialDialog.Builder(requireContext()).title(R.string.info)
                    .content(s).positiveText(R.string.ok).show();
        });

        btnInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickedButton = v;
                if (checkPermissions()) return;

                areYouSure(R.string.warningArchitecture,
                        new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                XposedZip selectedInstaller = (XposedZip) chooserInstallers.getSelectedItem();
                                Uri uri = Uri.parse(selectedInstaller.link);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        });
            }
        });

        btnUninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickedButton = v;
                if (checkPermissions()) return;

                areYouSure(R.string.warningArchitecture,
                        new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                XposedZip selectedUninstaller = (XposedZip) chooserUninstallers.getSelectedItem();
                                Uri uri = Uri.parse(selectedUninstaller.link);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        });
            }
        });

        noticeTv.setText(Html.fromHtml(notice()));
        author.setText(getString(R.string.download_author, author()));

        try {
            if (uninstallers().size() == 0) {
                infoUninstaller.setVisibility(View.GONE);
                chooserUninstallers.setVisibility(View.GONE);
                btnUninstall.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }

        if (!isStable()) {
            view.findViewById(R.id.warning_unstable).setVisibility(View.VISIBLE);
        }

        if (!isOfficial()) {
            view.findViewById(R.id.warning_unofficial).setVisibility(View.VISIBLE);
        }

        showOnXda.setOnClickListener(v -> NavUtil.startURL(getActivity(), supportUrl()));
        updateDescription.setOnClickListener(v -> new MaterialDialog.Builder(requireContext())
                .title(R.string.changes)
                .content(Html.fromHtml(description()))
                .positiveText(R.string.ok).show());

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_EXTERNAL_PERMISSION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mClickedButton != null) {
                    new Handler().postDelayed(() -> mClickedButton.performClick(), 500);
                }
            } else {
                Toast.makeText(getActivity(), R.string.permissionNotGranted, Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void areYouSure(int contentTextId, MaterialDialog.ButtonCallback yesHandler) {
        new MaterialDialog.Builder(requireActivity()).title(R.string.areyousure)
                .content(contentTextId)
                .iconAttr(android.R.attr.alertDialogIcon)
                .positiveText(android.R.string.yes)
                .negativeText(android.R.string.no).callback(yesHandler).show();
    }

}