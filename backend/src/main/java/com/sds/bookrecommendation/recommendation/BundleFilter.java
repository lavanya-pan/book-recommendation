package com.sds.bookrecommendation.recommendation;

import java.util.regex.Pattern;

/** Recognises titles that are bundles (box sets, collections) rather than single books. */
final class BundleFilter {

    private static final Pattern BUNDLE =
            Pattern.compile("box(ed)? ?set|collection|omnibus|treasury|study bible", Pattern.CASE_INSENSITIVE);

    private BundleFilter() {
    }

    static boolean isBundle(String title) {
        return title != null && BUNDLE.matcher(title).find();
    }
}
