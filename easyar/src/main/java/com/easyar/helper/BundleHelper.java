package com.easyar.helper;

import android.os.Bundle;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BundleHelper {

    public static class Builder {
        private Bundle extras;

        public Builder putExtras(Bundle extras) {
            if (this.extras != null)
                this.extras.putAll(extras);
            else
                this.extras = extras;
            return this;
        }

        public Builder putExtra(String key, Serializable object) {
            checkIfNotNull();
            this.extras.putSerializable(key, object);
            return this;
        }

        public Builder putExtra(String key, List<String> object) {
            checkIfNotNull();
            this.extras.putStringArrayList(key, new ArrayList<>(object));
            return this;
        }

        public Builder putExtra(String key, ArrayList<String> object) {
            checkIfNotNull();
            this.extras.putStringArrayList(key, object);
            return this;
        }

        public Builder putExtra(String key, Parcelable object) {
            checkIfNotNull();
            this.extras.putParcelable(key, object);
            return this;
        }

        private void checkIfNotNull() {
            if (this.extras == null)
                this.extras = new Bundle();
        }

        public Builder putExtra(String key, String object) {
            checkIfNotNull();
            this.extras.putString(key, object);
            return this;
        }

        public Builder putExtra(String key, boolean object) {
            checkIfNotNull();
            this.extras.putBoolean(key, object);
            return this;
        }

        public Builder putExtra(String key, int object) {
            checkIfNotNull();
            this.extras.putInt(key, object);
            return this;
        }

        public Builder putSerializableExtra(String key, Serializable object) {
            checkIfNotNull();
            this.extras.putSerializable(key, object);
            return this;
        }

        public Bundle get() {
            return extras;
        }

    }

}
