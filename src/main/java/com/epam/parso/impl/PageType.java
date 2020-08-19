package com.epam.parso.impl;

/**
 * Enumeration of all page types used in sas7bdat files.
 */
public enum PageType {
    /**
     * The page type storing metadata as a set of subheaders. It can also store compressed row data in subheaders.
     * The sas7bdat format has two values that correspond to the page type 'meta':
     * {@link SasFileConstants#PAGE_META_TYPE_1} and {@link SasFileConstants#PAGE_META_TYPE_2}.
     */
    PAGE_TYPE_META {
        @Override
        boolean contains(int value) {
            return SasFileConstants.PAGE_META_TYPE_1 == value || SasFileConstants.PAGE_META_TYPE_2 == value
                    || SasFileConstants.PAGE_CMETA_TYPE == value;
        }
    },
    /**
     * The page type storing metadata as a set of subheaders and data as a number of table rows.
     * The sas7bdat format has two values that correspond to the page type 'mix':
     * {@link SasFileConstants#PAGE_MIX_TYPE_1} and {@link SasFileConstants#PAGE_MIX_TYPE_2}.
     */
    PAGE_TYPE_MIX {
        @Override
        boolean contains(int value) {
            return SasFileConstants.PAGE_MIX_TYPE_1 == value || SasFileConstants.PAGE_MIX_TYPE_2 == value;
        }
    },
    /**
     * The page type amd. The value that correspond to the page type 'amd' is {@link SasFileConstants#PAGE_AMD_TYPE}.
     */
    PAGE_TYPE_AMD {
        @Override
        boolean contains(int value) {
            return SasFileConstants.PAGE_AMD_TYPE == value;
        }
    },
    /**
     * The page type storing only data as a number of table rows. The value that correspond to the page type 'data' is
     * {@link SasFileConstants#PAGE_DATA_TYPE}.
     */
    PAGE_TYPE_DATA {
        @Override
        boolean contains(int value) {
            return SasFileConstants.PAGE_DATA_TYPE == value || SasFileConstants.PAGE_DATA_TYPE_2 == value;
        }
    };

    /**
     * The method to check if one of PageType enumeration contains the specified page type.
     *
     * @param value - the page type for check.
     * @return true if PageType enumeration contains the specified page type, false otherwise.
     */
    abstract boolean contains(int value);
}
