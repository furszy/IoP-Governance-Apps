package iop.org.iop_contributors_app.wallet.util;

import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TypefaceSpan;

import org.bitcoinj.core.Address;
import org.iop.WalletConstants;


/**
 * Created by mati on 26/12/16.
 */

public class WalletUtilsAndroid {


    public static Editable formatAddress(final Address address, final int groupSize, final int lineSize)
    {
        return formatHash(address.toBase58(), groupSize, lineSize);
    }

    public static Editable formatAddress(@Nullable final String prefix, final Address address, final int groupSize, final int lineSize)
    {
        return formatHash(prefix, address.toBase58(), groupSize, lineSize, WalletConstants.CHAR_THIN_SPACE);
    }

    public static Editable formatHash(final String address, final int groupSize, final int lineSize)
    {
        return formatHash(null, address, groupSize, lineSize, WalletConstants.CHAR_THIN_SPACE);
    }

    public static Editable formatHash(@Nullable final String prefix, final String address, final int groupSize, final int lineSize,
                                      final char groupSeparator)
    {
        final SpannableStringBuilder builder = prefix != null ? new SpannableStringBuilder(prefix) : new SpannableStringBuilder();

        final int len = address.length();
        for (int i = 0; i < len; i += groupSize)
        {
            final int end = i + groupSize;
            final String part = address.substring(i, end < len ? end : len);

            builder.append(part);
            builder.setSpan(new TypefaceSpan("monospace"), builder.length() - part.length(), builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (end < len)
            {
                final boolean endOfLine = lineSize > 0 && end % lineSize == 0;
                builder.append(endOfLine ? '\n' : groupSeparator);
            }
        }

        return builder;
    }

}
