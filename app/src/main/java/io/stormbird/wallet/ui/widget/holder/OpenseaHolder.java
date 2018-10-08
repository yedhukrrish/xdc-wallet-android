package io.stormbird.wallet.ui.widget.holder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import io.reactivex.disposables.Disposable;
import io.stormbird.wallet.C;
import io.stormbird.wallet.R;
import io.stormbird.wallet.entity.ERC721Attribute;
import io.stormbird.wallet.entity.OpenseaElement;
import io.stormbird.wallet.service.OpenseaService;
import io.stormbird.wallet.ui.TokenDetailActivity;
import io.stormbird.wallet.util.KittyUtils;

/**
 * Created by James on 3/10/2018.
 * Stormbird in Singapore
 */
public class OpenseaHolder extends BinderViewHolder<OpenseaElement> {

    public static final int VIEW_TYPE = 1302;

    private final TextView titleText;
    private final ImageView image;
    private final TextView generation;
    private final TextView cooldown;
    private final TextView statusText;
    private final LinearLayout layoutDetails;
    private final OpenseaService openseaService;

    @Nullable
    private Disposable queryService;

    public OpenseaHolder(int resId, ViewGroup parent, OpenseaService service) {
        super(resId, parent);
        titleText = findViewById(R.id.name);
        image = findViewById(R.id.image_view);
        generation = findViewById(R.id.generation);
        cooldown = findViewById(R.id.cooldown);
        statusText = findViewById(R.id.status);
        layoutDetails = findViewById(R.id.layout_details);
        openseaService = service;
    }

    @Override
    public void bind(@Nullable OpenseaElement element, @NonNull Bundle addition)
    {
        //for now add title and ERC721 graphic
        String assetName;
        if (element.name != null && !element.name.equals("null"))
        {
            assetName = element.name;
        }
        else
        {
            assetName = "ID# " + String.valueOf(element.tokenId);
        }
        titleText.setText(assetName);

        ERC721Attribute gen = element.traits.get("generation");
        if (gen != null) {
            generation.setText(String.format("Gen %s", gen.attributeValue));
        }

        ERC721Attribute cooldownIndex = element.traits.get("cooldown_index");
        if (cooldownIndex != null) {
            cooldown.setText(String.format("%s Cooldown", KittyUtils.parseCooldownIndex(cooldownIndex.attributeValue)));
        }

        //now add the graphic
        Glide.with(getContext())
            .load(element.imageUrl)
            .into(image);

        image.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), TokenDetailActivity.class);
            intent.putExtra("element", element);
            getContext().startActivity(intent);
        });
    }

    private void setStatus(C.TokenStatus status) {
        if (status == C.TokenStatus.PENDING) {
            statusText.setVisibility(View.VISIBLE);
            statusText.setBackgroundResource(R.drawable.background_status_pending);
            statusText.setText(R.string.status_pending);
        } else if (status == C.TokenStatus.INCOMPLETE){
            statusText.setVisibility(View.VISIBLE);
            statusText.setBackgroundResource(R.drawable.background_status_incomplete);
            statusText.setText(R.string.status_incomplete_data);
        } else {
            statusText.setVisibility(View.GONE);
        }
    }
}
