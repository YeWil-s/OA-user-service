package com.oa.user.vo;

import java.util.List;

public class RouterVO {

    private String name;
    private String path;
    private String component;
    private String icon;
    private Integer menuType;
    private Meta meta;
    private List<RouterVO> children;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getMenuType() { return menuType; }
    public void setMenuType(Integer menuType) { this.menuType = menuType; }
    public Meta getMeta() { return meta; }
    public void setMeta(Meta meta) { this.meta = meta; }
    public List<RouterVO> getChildren() { return children; }
    public void setChildren(List<RouterVO> children) { this.children = children; }

    public static class Meta {
        private String title;
        private String icon;
        private boolean hidden;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public boolean isHidden() { return hidden; }
        public void setHidden(boolean hidden) { this.hidden = hidden; }
    }
}
