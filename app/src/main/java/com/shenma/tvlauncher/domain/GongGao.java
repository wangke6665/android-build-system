package com.shenma.tvlauncher.domain;

public class GongGao {
    private int code;
    private GongGaoData msg;

    public int getCode() {
        return this.code;
    }

    public void setCode(int i) {
        this.code = i;
    }

    public GongGaoData getMsg() {
        return this.msg;
    }

    public void setMsg(GongGaoData gongGaoData) {
        this.msg = gongGaoData;
    }

    public class GongGaoData {
        private Notice game_notice;
        private Notice home_notice;
        private Notice notice;
        private Notice registerd;
        private Notice roll_notice;
        private Notice un_register;

        public Notice getUn_register() {
            return this.un_register;
        }

        public void setUn_register(Notice notice) {
            this.un_register = notice;
        }

        public Notice getRegisterd() {
            return this.registerd;
        }

        public void setRegisterd(Notice notice) {
            this.registerd = notice;
        }

        public Notice getNotice() {
            return this.notice;
        }

        public void setNotice(Notice notice) {
            this.notice = notice;
        }

        public Notice getRoll_notice() {
            return this.roll_notice;
        }

        public void setRoll_notice(Notice notice) {
            this.roll_notice = notice;
        }

        public Notice getHome_notice() {
            return this.home_notice;
        }

        public void setHome_notice(Notice notice) {
            this.home_notice = notice;
        }

        public Notice getGame_notice() {
            return this.game_notice;
        }

        public void setGame_notice(Notice notice) {
            this.game_notice = notice;
        }
    }

    public class Notice {
        private String content;
        private String status;
        private String title;
        private String type;

        public String getStatus() {
            return this.status;
        }

        public void setStatus(String str) {
            this.status = str;
        }

        public String getTitle() {
            return this.title;
        }

        public void setTitle(String str) {
            this.title = str;
        }

        public String getContent() {
            return this.content;
        }

        public void setContent(String str) {
            this.content = str;
        }

        public String getType() {
            return this.type;
        }

        public void setType(String str) {
            this.type = str;
        }
    }
}
