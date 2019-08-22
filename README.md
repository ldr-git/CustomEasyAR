# CustomEasyAR

[![](https://jitpack.io/v/ldr-git/CustomEasyAR.svg)](https://jitpack.io/#ldr-git/CustomEasyAR) [![](https://img.shields.io/github/issues/ldr-git/CustomEasyAR)](https://github.com/ldr-git/CustomEasyAR/issues)

CustomEasyAR is an Android library for dealing with EasyAR library.

## Installation

```gradle
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

```gradle
dependencies {
	        implementation 'com.github.ldr-git:CustomEasyAR:v0.5.0'
	}
```

## Usage

### Image Recognition
```java
Bundle extras = new Bundle();
extras.putString(ImageTargetActivity.TARGET_KEY, "<TARGET NAME>");//Required
extras.putString(ImageTargetActivity.TARGET_PATH, "<PATH TO YOUR TARGET>");//Required
Intent intent = new Intent(YourActivity.this, TargetActivity.class);//TargetActivity is extending ImageTargetActivity
intent.putExtras(extras);
startActivity(intent);
```

### Image Recognition with Video Overlay
```java
Bundle extras = new Bundle();
extras.putString(VideoEasyARActivity.TARGET_KEY, "target");//Required
extras.putString(VideoEasyARActivity.TARGET_PATH, "<PATH TO YOUR TARGET>");//Required
extras.putString(VideoEasyARActivity.TARGET_VIDEO_OVERLAY, "<PATH TO YOUR TARGET OVERLAY VIDEO>");//Required
Intent intent = new Intent(YourActivity.this, TargetActivity.class);//TargetActivity is extending VideoTargetActivity
intent.putExtras(extras);
startActivity(intent);
```

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License
[MIT](https://choosealicense.com/licenses/mit/)
