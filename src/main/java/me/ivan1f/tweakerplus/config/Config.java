package me.ivan1f.tweakerplus.config;

import fi.dy.masa.malilib.interfaces.IStringValue;
import fi.dy.masa.malilib.util.StringUtils;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {
    Type value();

    Category category() default Category.MC_TWEAKS;

    /**
     * Any of these restrictions satisfied => enable
     */
    Restriction[] restriction() default {};

    boolean debug() default false;

    boolean devOnly() default false;

    enum Type implements IStringValue {
        GENERIC, HOTKEY, LIST, TWEAK, DISABLE;

        @Override
        public String getStringValue() {
            return StringUtils.translate("tweakerplus.gui.config_type." + this.name().toLowerCase());
        }
    }

    enum Category {
        MC_TWEAKS, MOD_TWEAKS, SETTING;

        public String getDisplayName() {
            return StringUtils.translate("tweakerplus.gui.config_category." + this.name().toLowerCase());
        }
    }
}
