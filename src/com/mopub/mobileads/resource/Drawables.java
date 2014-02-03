/*
 * Copyright (c) 2010-2013, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mopub.mobileads.resource;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import com.mopub.mobileads.util.Base64;
import com.mopub.mobileads.util.Dips;

import java.io.ByteArrayInputStream;


public enum Drawables {
	BACKGROUND("iVBORw0KGgoAAAANSUhEUgAAAAEAAAAsCAIAAAArRUU2AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAEFJREFUeNpicPP0Zvr3/z/T/3//gDQQg+i//5j+gum/QBqIQXwg+x+YjckH6fkL0/f3NwMPHz8jKxsbAw0AQIABAGYHPKslk98oAAAAAElFTkSuQmCC"),
	DEFAULT_VIDEO_POSTER("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"),

	LEFT_ARROW("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"),
	UNLEFT_ARROW("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"),
	RIGHT_ARROW("iVBORw0KGgoAAAANSUhEUgAAABEAAAAUCAYAAABroNZJAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAq5JREFUeNqUlN9LmmEUx4++/ihKy6yZGEMjlExdEjgqQmm78ioGu41gu7Ftt+5mBF0Go8sIBvsTtggqGgODboK66aIwNi0lJ7p+2TT89b7Pvo/LaKucHTj4+LzP+bzne855XpnVan0ZiUSSoiimiCgBP4UXfD4fozpsbW2NhP7+/vdDQ0MvFLBkMnmBfQEuHhwclOHMbDbXhOAMCQ6HY8Lr9Tr8fv9ji8Xy6PDw8CKTyUh4LoNzUBkg9l/I4OCg2ePxkNPpfOB2u0cbGhoeQuJZsVjkWbHLrMTbYBVIb2/vhMvlMttsNurs7CSj0Si32+0W7D0BRMthOKuAS5cw6brEvyCQQk1NTdTY2EhtbW3U1dWlxr6rp6dnJJVKldLpdBEx8kuJYrVeNyAcwE0QBGpubqb29nbq7u7WDgwMjABs39/fz+RyOV4rVoXx9a0QbjKZjJRKJWm1WjIYDIRRMKJeT/ECQzQazZTLZZ4Vb0D5Tsh1mFqtJp1ORyaTSejr67MixouMFLFY7AeOnCuoTsMYkV6vJ41GwxugR1cnNzY2POvr66/qhlzPChlRS0tLHuuv6F74XpCqYRg/7e7uvh0eHo7wwsrvE5zP53cQPIr6PMcV+T41NVUZvrog6MTPeDz+BoXn1yI0MzMjLS8v09bW1p961QpmjJWOj48/rKysvBsfHz+bm5tji4uLBBBls1nCzaeamUD3F1Tf3dHR8RoFPQ0GgywUCtHe3h5/dgWoQtg/ur+Fw+Fnra2tfoz7zvT0NFtdXaXt7W06Ojri0m68UI7+5/lCkqRfiUQiGAgEnPj9PDs7K3Ldm5ubhP9UKBS4vFuzlqtUqnPo+7i0tGTFpeMfqALXjSEijHdF+13BV/MzNjamWVhYyM7PzzOMcSXw5OSESqUS1ft5/C3AAL39YeI2ufApAAAAAElFTkSuQmCC"),
	UNRIGHT_ARROW("iVBORw0KGgoAAAANSUhEUgAAABEAAAAUCAYAAABroNZJAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAqNJREFUeNqUlEtrE1EUx8+8EjNT27zTaMw0k6RJGpu+VqIi4k7rQtyKIn6FrEraRhTET+AnENxUXLpWcFfRhaCbCpY0Lc3DpE07c+dxPTckojTG5sAfhnvP/fE/99wz3IV4/Fl1Z+eDZVlVAKigmihjwuejcIpoNZsg+AOBlz6//2E4EonWazUGElC2oesWip7xeIZCMAeEUDhcpBw3LkqubDqdvs1xQNqt1iHucygGshBEh0LQQREBY2fHx8F2HLfkcl1OJKaut1vtCiEGy6M9V/YgWBcSDIWKHM+PyYoMLpcbJEkCYpoT/mDg1rloNF+tVrcwV0Q5PZjzZ4l/QdiGIAjAoxiI43jOtKyYOqXeVRRFrmPgGb5Xot2/rxMQnhe6dA4vRhDFLsymjkApzGta8iYhpNnpdAgrsQ9j3wMh/cB1EAURy3QBsSyPMqbcUFX1UrPRqJimyUAOgw2F9GzhOsLQFXNHiBmMRCeXg8FgfG939wtmtLnszMwuJ4gRr98Hoij993E5jgPoAvSjI1A8ngPT0B+JMGIwV263G2RZhulU8lNle/vdyBAWc4XCdijgv7deKr1nFzsSJB6L6ZnMdHFtZeVFbrbgxBMa/Pi+BaeCeL1eZ3529tWT8vr9iwsLdiKVhka9BqZBuvtDIRJ2Y3Fh/jM4dBkBlVQmSxv7NTAMHWzbBkrpcEg+l9ufjIQflFdX32byeRpTVfiJY29Z5u/D/TgBOR+Nkmw287xcKj2eKczZqqZBs97AthKg2N5B48zjvLOnCzJO6LWrV95g330bG6/XtPS0zeo+aLeBoH3nH4CuE5wTktS0r1pSu/O0XP6WzuZop3OIg2Vg3dYJ64OCW1xamvy4ubmXwsOGfgz68TFY+CIdeqq/Y/f3+EuAAQARwzy3ZhCNHQAAAABJRU5ErkJggg=="),
	REFRESH("iVBORw0KGgoAAAANSUhEUgAAABMAAAAUCAYAAABvVQZ0AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA2NJREFUeNqMVF1Ik1EYPm7TpsIca84pGs6VrboYaeRPpFgXKUzBH4S8sLoIMVCpRERCAgfSZXpR2ZVJCEIgaIQm+MdCIkXRTTTnQG0qorCFm21zp+f9+IQ5NHvh4eP7vnOe877Ped5Xyv4dEYAMSK2rq/tkt9tlXq83Cu9BwC8+zyQ4B5wHLgAG4M7IyAgfHR31FhcXv8F7EXAZUADSo43SMCLKQimS0GKDTqfLys7OvlldXZ2Vnp4uy8nJuZGWlmacnp52ejyeANZ4AB/AQ4kigSQgT6/XPzebzd8pk4WFBb6+vs6xkVP4/X6+s7PDx8bGPJWVlWasTxGTOJZRInC3oaHh88zMjLDh4OCA+3w+4RkIBPhRHB4e8u3tbd7f38+rqqpM2CcJ1YhKu4Vsvq2urnKIzN1uN3c4HHxqaso1Pj7+Y3d3VyAicqfTyaHhenNzc1Eo0VF5F2traz+srKwIRFtbW3xoaMjW1tZ2X9Q12WazcZfLxefn53lXV9fbiooKjZjIsVAkJSXdIw1IFyqvt7d3pLy8PCFksXJgYIAPDg5aGxsbs8OzCY2E+vr692tra4LIFovld15eXmrYqbKmpiZzuNAnRXJ3d7eVStjc3OQo7ZXos7OC5ImjZAA1EE0nSbRarUYulzNoxeCfb/gW+A+yGEAvuoC8ZieyoFKpVEulUgYPMVy3Qyzn8IwuiYWNXl5FQB4/9r0gIf+gxG34iEVFRbH8/HwSOPakmwovMTMz81JZWZm+oKDAkJiYGENk3o2NjSVYgsXFxbHc3NxCMXX5KYRkFRU2XzEajQZUxSQSCdvb2/tJZAfLy8uDyI4pFApmMpkKNRpNJr7rxEaWiaS0NhrQAuk1NTVPoDWDiRmS2RweHrYK9aO0FPjMvb+/z3EC7+vr+6VWqx/j321xauhEsa8DJjR9NxIQDI6xxFtbW58emx4tLS0PFhcXhT4kwomJCS/67qNKparH/4fAo4yMDHNHR8c8EdDB1J8w+NRJVors7Ox8t7S0JCwkIH1utVr57Owsn5ub49RudBAdSC2HG7SVlJRoT7ssaXt7e+3k5KSLiIiQJgRFMBgUmpzIqE97enq+lJaWhrbciYwRKE+NW32GIVgcHx9/TSaTMfAxkLsx2ywge40J8zV8bP8VYACAQuluULZPjQAAAABJRU5ErkJggg=="),
	CLOSE("iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAg9JREFUeNp8kk1rWkEUhodc5aJUEbVqJQRcqCUgERottIKYQhdZSEh1pcWFSFd2HdzFhfobpOJCFLEbddONEUUQEUSDxi4KtWIq+LGQanOLFqdnJAO3peTAe+98PXPmvDMMQkgE0ikUilcajca2WCx+Q58DbUAYJARpQccmk+l8Op1CE/0inycqlcqdTCZvi8XincViCcHYEUgKYkH7oNfhcLhRKpWw1+v9AH0zMhgMh6lU6utkMsGQDddqNQo/Az0lUCQSaQyHQ7xcLnGn08GBQOCSsdlsKtB7nU7HSqVSJJfLhWaz+UWv15uPx2M2Go1eeDye51qtFgkEAgQwgvEv5Kh7wWDwZb1e/wGBt9stJv9Wq8Wl0+mb0WiEN5vNTiRrIpHI3deN/guv12vMcdyDEA3W5XKdtdvtnwTix3w+x/F4/BOsUZAkfEhI3ctms9ckEz+gJmy32/lu7/0FgRENWhM/VqsV320Ko8egE2o5rYkcj2QiEDWMwFar9QLWHxLwIBaLlfkQaWcymVwoFLL/63alUrlzOBxvGTDkyOl0Xur1+t09QRYEL+Sjz+fzQIZvYrG4qlar3TKZjBWJRLvSYPNHTL/fvzUajTOlUnlKLrdareb8fr+XvtVms/mdwDDvhqxst9v9nM/nzxgyWS6XW7DreDAYDOE+391DNHawRCK5ms1mmkKh8AY0/SPAAEsFgVbY4GziAAAAAElFTkSuQmCC"),

	INTERSTITIAL_CLOSE_BUTTON_NORMAL("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"),
	INTERSTITIAL_CLOSE_BUTTON_PRESSED("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");

	private final String encodedString;
	private BitmapDrawable cachedImage;

	private Drawables(String encodedString) {
		this.encodedString = encodedString;
	}

	public BitmapDrawable decodeImage(Context context) {
		if (cachedImage == null) {
			DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
			int scaledDensity = Dips.asIntPixels(displayMetrics.xdpi, context);

			byte[] rawImageData = Base64.decode(encodedString, Base64.DEFAULT);
			cachedImage = new BitmapDrawable(new ByteArrayInputStream(rawImageData));
			cachedImage.setTargetDensity(scaledDensity);
		}

		return cachedImage;
	}
}
