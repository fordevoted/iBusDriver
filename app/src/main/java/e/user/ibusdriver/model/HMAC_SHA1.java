package e.user.ibusdriver.model;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMAC_SHA1 {
	private static final char[] toBase64 = {
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
			'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
			'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
	};

	public static String Signature(String xData, String AppKey) {
		try {
			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(AppKey.getBytes("UTF-8"),"HmacSHA1");

			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);

			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(xData.getBytes("UTF-8"));
			byte[] encoded = encodeToStringByBase64EncodeToString(rawHmac);
			return new String(encoded);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	private static String bytesToHex(byte[] bytes) {
		char[] hexArray = "0123456789abcdef".toCharArray();
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}


	public static  byte[] encodeToStringByBase64EncodeToString(byte[] src) {
		int len = outLength(src.length);          // dst array size
		byte[] dst = new byte[len];
		int ret = encode0(src, 0, src.length, dst);
		if (ret != dst.length)
			return Arrays.copyOf(dst, ret);
		return dst;
	}
	private static final int outLength(int srclen) {
		return 4 * ((srclen + 2) / 3);
	}

	private static int encode0(byte[] src, int off, int end, byte[] dst) {
		char[] base64 = toBase64;
		int sp = off;
		int slen = (end - off) / 3 * 3;
		int sl = off + slen;
		int dp = 0;
		while (sp < sl) {
			int sl0 = Math.min(sp + slen, sl);
			for (int sp0 = sp, dp0 = dp ; sp0 < sl0; ) {
				int bits = (src[sp0++] & 0xff) << 16 |
						(src[sp0++] & 0xff) <<  8 |
						(src[sp0++] & 0xff);
				dst[dp0++] = (byte)base64[(bits >>> 18) & 0x3f];
				dst[dp0++] = (byte)base64[(bits >>> 12) & 0x3f];
				dst[dp0++] = (byte)base64[(bits >>> 6)  & 0x3f];
				dst[dp0++] = (byte)base64[bits & 0x3f];
			}
			int dlen = (sl0 - sp) / 3 * 4;
			dp += dlen;
			sp = sl0;
		}
		if (sp < end) {               // 1 or 2 leftover bytes
			int b0 = src[sp++] & 0xff;
			dst[dp++] = (byte)base64[b0 >> 2];
			if (sp == end) {
				dst[dp++] = (byte)base64[(b0 << 4) & 0x3f];
					dst[dp++] = '=';
					dst[dp++] = '=';
			} else {
				int b1 = src[sp++] & 0xff;
				dst[dp++] = (byte)base64[(b0 << 4) & 0x3f | (b1 >> 4)];
				dst[dp++] = (byte)base64[(b1 << 2) & 0x3f];
				dst[dp++] = '=';
			}
		}
		return dp;
	}
}