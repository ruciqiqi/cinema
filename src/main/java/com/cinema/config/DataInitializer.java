package com.cinema.config;

import com.cinema.entity.*;
import com.cinema.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DataInitializer implements CommandLineRunner {
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final SnackRepository snackRepository;
    private final CinemaRepository cinemaRepository;
    private final CouponRepository couponRepository;
    private final RefundRuleRepository refundRuleRepository;
    private final AnnouncementRepository announcementRepository;
    private NotificationRepository notificationRepository;

    public DataInitializer(MovieRepository movieRepository, HallRepository hallRepository,
                           ShowtimeRepository showtimeRepository, SeatRepository seatRepository,
                           UserRepository userRepository, SnackRepository snackRepository,
                           CinemaRepository cinemaRepository, CouponRepository couponRepository,
                           RefundRuleRepository refundRuleRepository,
                           AnnouncementRepository announcementRepository,
                           NotificationRepository notificationRepository) {
        this.movieRepository = movieRepository;
        this.hallRepository = hallRepository;
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
        this.snackRepository = snackRepository;
        this.cinemaRepository = cinemaRepository;
        this.couponRepository = couponRepository;
        this.refundRuleRepository = refundRuleRepository;
        this.announcementRepository = announcementRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void run(String... args) {
        boolean fresh = movieRepository.count() == 0;
        String today = LocalDate.now().toString();

        if (!fresh) {
            // Add coming soon movies if they don't exist yet
            long comingCount = movieRepository.findAll().stream()
                .filter(m -> "coming".equals(m.getStatus())).count();
            if (comingCount == 0) addComingMovies();
            return;
        }

        // ===== Cinemas =====
        String[][] cinemaData = {
            {"星空影城（朝阳旗舰店）", "北京市朝阳区建国路88号SKP购物中心B1", "010-88886666", "open"},
            {"星空影城（海淀店）", "北京市海淀区中关村大街15号", "010-66668888", "open"},
            {"星空影城（西城店）", "北京市西城区西单北大街130号", "010-55556666", "open"},
        };
        for (String[] c : cinemaData) {
            Cinema cinema = new Cinema();
            cinema.setName(c[0]); cinema.setAddress(c[1]);
            cinema.setPhone(c[2]); cinema.setStatus(c[3]);
            cinemaRepository.save(cinema);
        }

        // ===== Admin user =====
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(hashPassword("123456"));
        admin.setPhone("13800000000");
        admin.setRole("admin");
        admin.setMemberLevel(5);
        admin.setPoints(9999);
        admin.setCreatedAt("2026-05-07 00:00:00");
        userRepository.save(admin);

        // ===== Demo user =====
        User demo = new User();
        demo.setUsername("test");
        demo.setPassword(hashPassword("123456"));
        demo.setPhone("13900000001");
        demo.setRole("user");
        demo.setMemberLevel(1);
        demo.setPoints(150);
        demo.setTotalSpent(260.0);
        demo.setCreatedAt("2026-05-07 00:00:00");
        userRepository.save(demo);

        // ===== Movies =====
        String[][] movies = {
            {"流浪地球3", "郭帆", "吴京、刘德华、李雪健", "科幻/冒险", "178", "中文", "2026-02-10",
             "太阳即将毁灭，人类在地球表面建造出巨大的推进器，寻找新的家园。然而太空之路危机四伏，为了拯救地球，流浪地球时代的年轻人再次挺身而出，展开争分夺秒的生死之战。",
             "linear-gradient(135deg, #0c1445 0%, #1a3a6b 25%, #2d5fa5 50%, #4a90d9 75%, #7ab8f5 100%)", "9.2", "中国", "true", "5600000000",
             "/posters/liulangdiqiu3.svg"},
            {"封神第二部", "乌尔善", "费翔、黄渤、于适", "奇幻/史诗", "150", "中文", "2026-02-01",
             "殷商大军兵临城下，西岐军民在姜子牙的率领下团结一心。姬发从一名质子成长为西岐少主，率领众将迎战强大的殷商军团。",
             "linear-gradient(135deg, #3e0a0a 0%, #6b1515 25%, #a02020 50%, #c0392b 75%, #e74c3c 100%)", "8.9", "中国", "true", "3800000000",
             "/posters/fengshen2.svg"},
            {"哪吒之魔童闹海", "饺子", "吕艳婷、囧森瑟夫", "动画/奇幻", "120", "中文", "2026-02-01",
             "天劫之后，哪吒、敖丙的灵魂虽保住了，但肉身很快会魂飞魄散。太乙真人打算用七色宝莲给二人重塑肉身，过程中却遇到重重困难。",
             "linear-gradient(135deg, #d63031 0%, #e17055 25%, #fd79a8 50%, #fab1a0 75%, #ffeaa7 100%)", "9.5", "中国", "true", "7800000000",
             "/posters/nezha2.svg"},
            {"红海行动2", "林超贤", "张译、黄景瑜、海清", "动作/战争", "140", "中文", "2026-03-15",
             "蛟龙突击队再度集结，执行一项惊心动魄的撤侨任务。在异国他乡的战场上，他们面对的是更加凶险的敌人和更复杂的局势。",
             "linear-gradient(135deg, #1a1a1a 0%, #333 25%, #555 50%, #777 75%, #999 100%)", "8.7", "中国", "true", "2900000000",
             "/posters/honghai2.svg"},
            {"唐人街探案4", "陈思诚", "王宝强、刘昊然", "喜剧/悬疑", "135", "中文", "2026-03-01",
             "唐仁和秦风受邀前往伦敦，调查一起离奇的博物馆盗窃案。两人在异国街头展开爆笑又惊险的探案之旅。",
             "linear-gradient(135deg, #6c5ce7 0%, #a29bfe 25%, #fd79a8 50%, #fab1a0 75%, #ffeaa7 100%)", "8.5", "中国", "false", "2200000000",
             "/posters/tangtan4.svg"},
            {"志愿军：存亡之战", "陈凯歌", "辛柏青、朱一龙、张子枫", "战争/历史", "165", "中文", "2026-01-25",
             "铁原阻击战，志愿军63军以寡敌众，浴血奋战13个昼夜，用血肉之躯筑起钢铁长城，为新中国赢得了尊严与和平。",
             "linear-gradient(135deg, #4a2a0e 0%, #6b3a15 25%, #8b5a2a 50%, #a07040 75%, #c09060 100%)", "9.0", "中国", "false", "1800000000",
             "/posters/zhiyuanjun.svg"},
            {"熊出没·重启未来", "林汇达", "谭笑、张秉君", "动画/喜剧", "95", "中文", "2026-02-01",
             "光头强和熊大熊二意外进入未来世界，发现地球已被人工智能统治。他们必须找到回到过去的方法，改变未来。",
             "linear-gradient(135deg, #1a7a1a 0%, #2d9d2d 25%, #4ac24a 50%, #7ddb7d 75%, #a0f0a0 100%)", "8.3", "中国", "false", "900000000",
             "/posters/xiongchumo.svg"},
            {"深海2", "田晓鹏", "王亭文、苏鑫", "动画/奇幻", "130", "中文", "2026-03-08",
             "参宿再次潜入深海世界寻找母亲，这一次她发现了一个更大的秘密——深海世界正面临崩溃的危险，而她可能是唯一能拯救它的人。",
             "linear-gradient(135deg, #003366 0%, #004d99 25%, #0066cc 50%, #3399ff 75%, #66bbff 100%)", "8.8", "中国", "false", "1200000000",
             "/posters/shenhai2.svg"},
            {"射雕英雄传：侠之大者", "徐克", "肖战、庄达菲", "武侠/动作", "155", "中文", "2026-01-20",
             "郭靖和黄蓉联手守卫襄阳城，抵御外敌入侵。在战火纷飞的时代，他们用行动诠释了\"侠之大者，为国为民\"的真正含义。",
             "linear-gradient(135deg, #5c3a0e 0%, #8b5e3c 25%, #a07040 50%, #c09060 75%, #d4a870 100%)", "8.6", "中国", "false", "3200000000",
             "/posters/shediao.svg"},
            {"蜘蛛侠：纵横宇宙2", "乔伊姆·多斯·桑托斯", "沙梅克·摩尔、海莉·斯坦菲尔德", "动画/动作", "140", "英语", "2026-03-20",
             "迈尔斯·莫拉莱斯穿越多元宇宙，与来自不同时空的蜘蛛侠们组队。新反派\"斑点\"的出现威胁着整个多元宇宙的存亡。",
             "linear-gradient(135deg, #cc0000 0%, #e62e2e 25%, #1a1aff 50%, #0000cc 75%, #000066 100%)", "9.1", "美国", "true", "4500000000",
             "/posters/zhizhuxia2.svg"},
            {"阿凡达3：火与灰", "詹姆斯·卡梅隆", "萨姆·沃辛顿、佐伊·索尔达娜", "科幻/冒险", "190", "英语", "2026-04-01",
             "杰克和奈蒂莉遇到了纳美人的火族部落。这个充满敌意的全新部落拥有操控火焰的能力，他们的到来将永远改变潘多拉星球的力量平衡。",
             "linear-gradient(135deg, #cc3300 0%, #ff4d1a 25%, #ff6633 50%, #ff9933 75%, #ffcc00 100%)", "9.3", "美国", "true", "3500000000",
             "/posters/afanda3.svg"},
        };

        // Save showing movies
        addShowingMovies();
        addComingMovies();

        // ===== Halls (3 per cinema = 9 total) =====
        String[] hallNames = {"1号激光IMAX厅", "2号杜比全景声厅", "3号豪华VIP厅", "4号情侣厅", "5号普通厅", "6号IMAX厅", "7号杜比厅", "8号VIP厅", "9号普通厅"};
        String[] hallTypes = {"IMAX", "DOLBY", "VIP", "COUPLE", "STANDARD", "IMAX", "DOLBY", "VIP", "STANDARD"};
        int[][] hallConfigs = {{8,14},{8,12},{6,10},{8,8},{8,12},{8,14},{8,12},{6,10},{8,12}};
        Long[] cinemaIds = {1L, 1L, 1L, 2L, 2L, 2L, 3L, 3L, 3L};

        for (int h = 0; h < hallNames.length; h++) {
            Hall hall = new Hall();
            hall.setName(hallNames[h]);
            hall.setHallType(hallTypes[h]);
            hall.setCinemaId(cinemaIds[h]);
            hall.setRows(hallConfigs[h][0]);
            hall.setSeatCols(hallConfigs[h][1]);
            hall = hallRepository.save(hall);

            for (int r = 0; r < hallConfigs[h][0]; r++) {
                char rowChar = (char) ('A' + r);
                String rowLabel = String.valueOf(rowChar);
                for (int c = 1; c <= hallConfigs[h][1]; c++) {
                    Seat seat = new Seat();
                    seat.setHallId(hall.getId());
                    seat.setRowLabel(rowLabel);
                    seat.setSeatNum(c);
                    String ht = hallTypes[h];
                    if ("VIP".equals(ht)) {
                        seat.setSeatType(r >= hallConfigs[h][0] - 2 ? "vip" : "standard");
                    } else if ("IMAX".equals(ht)) {
                        seat.setSeatType(r >= hallConfigs[h][0] - 3 ? "vip" : "standard");
                    } else if ("COUPLE".equals(ht)) {
                        seat.setSeatType("vip"); // all couple seats are VIP
                    } else {
                        seat.setSeatType("standard");
                    }
                    seatRepository.save(seat);
                }
            }
        }

        // ===== Showtimes (use first 3 halls for simplicity — lots of showtimes) =====
        String tomorrow = LocalDate.now().plusDays(1).toString();
        String dayAfter = LocalDate.now().plusDays(2).toString();
        String[] dates = {today, tomorrow, dayAfter};
        String[][] timeSlots = {
            {"10:00", "13:30", "17:00", "20:30"},
            {"11:00", "14:30", "18:30", "21:00"},
            {"10:30", "14:00", "19:00", "21:30"}
        };
        double[][] priceConfigs = {{35.9, 55.9}, {29.9, 49.9}, {59.9, 89.9}};

        java.util.Random rnd = new java.util.Random();
        for (Movie movie : movieRepository.findAll()) {
            for (int h = 0; h < 3; h++) {
                for (String date : dates) {
                    int start = rnd.nextInt(timeSlots[h].length - 2);
                    for (int t = start; t < start + 3 && t < timeSlots[h].length; t++) {
                        Showtime st = new Showtime();
                        st.setMovieId(movie.getId());
                        st.setHallId(Long.valueOf(h + 1));
                        st.setHallName(hallNames[h]);
                        st.setShowDate(date);
                        st.setShowTime(timeSlots[h][t]);
                        st.setPriceStandard(priceConfigs[h][0]);
                        st.setPriceVip(priceConfigs[h][1]);
                        showtimeRepository.save(st);
                    }
                }
            }
        }

        // ===== Snacks =====
        String[][] snackData = {
            {"爆米花（小）", "小食", "18.0", "#f39c12"},
            {"爆米花（中）", "小食", "25.0", "#e67e22"},
            {"爆米花（大）", "小食", "32.0", "#d35400"},
            {"可乐（中）", "饮料", "10.0", "#e74c3c"},
            {"可乐（大）", "饮料", "15.0", "#c0392b"},
            {"雪碧（中）", "饮料", "10.0", "#2ecc71"},
            {"橙汁", "饮料", "12.0", "#f39c12"},
            {"矿泉水", "饮料", "5.0", "#3498db"},
            {"热狗", "热食", "15.0", "#e74c3c"},
            {"鸡米花", "热食", "20.0", "#d35400"},
            {"薯条", "热食", "16.0", "#f39c12"},
            {"冰淇淋", "甜品", "12.0", "#9b59b6"},
            {"巧克力", "零食", "18.0", "#795548"},
            {"双人套餐（2可乐+1爆米花）", "套餐", "45.0", "#e91e63"},
            {"家庭套餐（3可乐+2爆米花+鸡米花）", "套餐", "88.0", "#ff5722"},
        };
        for (String[] s : snackData) {
            Snack snack = new Snack();
            snack.setName(s[0]); snack.setCategory(s[1]);
            snack.setPrice(Double.parseDouble(s[2]));
            snack.setImage(s[3]); snack.setStatus("on");
            snackRepository.save(snack);
        }

        // ===== Refund Rules =====
        int[][] rules = {{24, 100}, {12, 80}, {6, 50}, {0, 0}};
        String[] ruleDescs = {"开场前24小时以上，全额退款", "开场前12-24小时，退款80%", "开场前6-12小时，退款50%", "开场前6小时内，不可退款"};
        for (int i = 0; i < rules.length; i++) {
            RefundRule rule = new RefundRule();
            rule.setHoursBeforeShow(rules[i][0]);
            rule.setRefundRate(rules[i][1] / 100.0);
            rule.setDescription(ruleDescs[i]);
            refundRuleRepository.save(rule);
        }

        // ===== Coupons =====
        String[][] couponData = {
            {"NEWUSER10", "新用户专享9折券", "discount", "10", "0", today, LocalDate.now().plusDays(30).toString(), "500", "active"},
            {"SUMMER20", "暑期观影满减券", "cash", "20", "50", today, LocalDate.now().plusDays(60).toString(), "200", "active"},
            {"VIP50", "会员专属5折券", "discount", "50", "0", today, LocalDate.now().plusDays(90).toString(), "100", "active"},
            {"WELCOME5", "新人5元代金券", "cash", "5", "0", today, LocalDate.now().plusDays(30).toString(), "999", "active"},
        };
        for (String[] co : couponData) {
            Coupon coupon = new Coupon();
            coupon.setCode(co[0]); coupon.setName(co[1]);
            coupon.setType(co[2]); coupon.setValue(Double.parseDouble(co[3]));
            coupon.setMinAmount(Double.parseDouble(co[4]));
            coupon.setStartDate(co[5]); coupon.setEndDate(co[6]);
            coupon.setUsageLimit(Integer.parseInt(co[7]));
            coupon.setUsedCount(0); coupon.setStatus(co[8]);
            couponRepository.save(coupon);
        }

        // ===== Announcements =====
        String[][] annData = {
            {"系统升级通知", "亲爱的影迷朋友，星空影院将于2026年5月10日凌晨2:00-4:00进行系统升级维护，届时在线购票功能将暂时无法使用，敬请谅解。"},
            {"《哪吒之魔童闹海》票房破78亿", "恭喜《哪吒之魔童闹海》全球票房突破78亿人民币，成为中国影史票房冠军！即日起至5月31日，凭此片票根可享受卖品8折优惠。"},
            {"五一档观影指南", "五一假期即将到来，星空影院为您精心准备了多部精彩大片：《阿凡达3》《蜘蛛侠：纵横宇宙2》《唐人街探案4》等火热上映中，快来选座购票吧！"},
        };
        for (String[] a : annData) {
            Announcement ann = new Announcement();
            ann.setTitle(a[0]); ann.setContent(a[1]);
            ann.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ann.setStatus("published");
            announcementRepository.save(ann);
        }

        // ===== Notifications (per-user) =====
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Admin notifications
        Long[][] adminNotifs = {{1L, 1L}, {1L, 2L}};
        for (Long[] na : adminNotifs) {
            User u = userRepository.findById(na[0]).orElse(null);
            if (u != null) {
                String[][] items = {
                    {"系统通知", "欢迎登录后台管理系统", "/admin"},
                    {"安全提醒", "请定期修改管理密码，确保系统安全", "/user/center?tab=security"},
                };
                for (String[] item : items) {
                    Notification n = new Notification();
                    n.setUserId(u.getId());
                    n.setTitle(item[0]); n.setContent(item[1]); n.setActionUrl(item[2]);
                    n.setIsRead(false); n.setCreatedAt(now);
                    notificationRepository.save(n);
                }
            }
        }

        // Test user notifications
        Long[][] testNotifs = {{2L, 1L}, {2L, 2L}};
        for (Long[] tu : testNotifs) {
            User u = userRepository.findById(tu[0]).orElse(null);
            if (u != null) {
                String[][] items = {
                    {"购票成功", "您已成功购买《哪吒之魔童闹海》电影票", "/my/orders"},
                    {"优惠券到账", "新用户专享9折券已到账，快去使用吧", "/user/center"},
                    {"观影提醒", "您预定的影片将在明天19:00开场", "/my/orders"},
                };
                for (String[] item : items) {
                    Notification n = new Notification();
                    n.setUserId(u.getId());
                    n.setTitle(item[0]); n.setContent(item[1]); n.setActionUrl(item[2]);
                    n.setIsRead(false); n.setCreatedAt(now);
                    notificationRepository.save(n);
                }
            }
        }

        System.out.println("=== 影院数据初始化完成 ===");
        System.out.println("影院: " + cinemaRepository.count() + " 家");
        System.out.println("影片: " + movieRepository.count() + " 部");
        System.out.println("影厅: " + hallRepository.count() + " 个");
        System.out.println("场次: " + showtimeRepository.count() + " 场");
        System.out.println("卖品: " + snackRepository.count() + " 种");
        System.out.println("优惠券: " + couponRepository.count() + " 种");
        System.out.println("通知: " + notificationRepository.count() + " 条");
        System.out.println("用户: " + userRepository.count() + " 个");
        System.out.println("管理员: admin / 123456");
        System.out.println("测试用户: test / 123456");
    }

    private void addShowingMovies() {
        String[][] movies = {
            {"流浪地球3", "郭帆", "吴京、刘德华、李雪健", "科幻/冒险", "178", "中文", "2026-02-10", "太阳即将毁灭，人类在地球表面建造出巨大的推进器，寻找新的家园。", "linear-gradient(135deg, #0c1445 0%, #1a3a6b 25%, #2d5fa5 50%, #4a90d9 75%, #7ab8f5 100%)", "9.2", "中国", "true", "5600000000", "/posters/liulangdiqiu3.svg"},
            {"封神第二部", "乌尔善", "费翔、黄渤、于适", "奇幻/史诗", "150", "中文", "2026-02-01", "殷商大军兵临城下，西岐军民在姜子牙的率领下团结一心。", "linear-gradient(135deg, #3e0a0a 0%, #6b1515 25%, #a02020 50%, #c0392b 75%, #e74c3c 100%)", "8.9", "中国", "true", "3800000000", "/posters/fengshen2.svg"},
            {"哪吒之魔童闹海", "饺子", "吕艳婷、囧森瑟夫", "动画/奇幻", "120", "中文", "2026-02-01", "天劫之后，哪吒、敖丙的灵魂虽保住了，但肉身很快会魂飞魄散。", "linear-gradient(135deg, #d63031 0%, #e17055 25%, #fd79a8 50%, #fab1a0 75%, #ffeaa7 100%)", "9.5", "中国", "true", "7800000000", "/posters/nezha2.svg"},
            {"红海行动2", "林超贤", "张译、黄景瑜、海清", "动作/战争", "140", "中文", "2026-03-15", "蛟龙突击队再度集结，执行一项惊心动魄的撤侨任务。", "linear-gradient(135deg, #1a1a1a 0%, #333 25%, #555 50%, #777 75%, #999 100%)", "8.7", "中国", "true", "2900000000", "/posters/honghai2.svg"},
            {"唐人街探案4", "陈思诚", "王宝强、刘昊然", "喜剧/悬疑", "135", "中文", "2026-03-01", "唐仁和秦风受邀前往伦敦，调查一起离奇的博物馆盗窃案。", "linear-gradient(135deg, #6c5ce7 0%, #a29bfe 25%, #fd79a8 50%, #fab1a0 75%, #ffeaa7 100%)", "8.5", "中国", "false", "2200000000", "/posters/tangtan4.svg"},
            {"志愿军：存亡之战", "陈凯歌", "辛柏青、朱一龙、张子枫", "战争/历史", "165", "中文", "2026-01-25", "铁原阻击战，志愿军63军以寡敌众，浴血奋战13个昼夜。", "linear-gradient(135deg, #4a2a0e 0%, #6b3a15 25%, #8b5a2a 50%, #a07040 75%, #c09060 100%)", "9.0", "中国", "false", "1800000000", "/posters/zhiyuanjun.svg"},
            {"熊出没·重启未来", "林汇达", "谭笑、张秉君", "动画/喜剧", "95", "中文", "2026-02-01", "光头强和熊大熊二意外进入未来世界，发现地球已被人工智能统治。", "linear-gradient(135deg, #1a7a1a 0%, #2d9d2d 25%, #4ac24a 50%, #7ddb7d 75%, #a0f0a0 100%)", "8.3", "中国", "false", "900000000", "/posters/xiongchumo.svg"},
            {"深海2", "田晓鹏", "王亭文、苏鑫", "动画/奇幻", "130", "中文", "2026-03-08", "参宿再次潜入深海世界寻找母亲。", "linear-gradient(135deg, #003366 0%, #004d99 25%, #0066cc 50%, #3399ff 75%, #66bbff 100%)", "8.8", "中国", "false", "1200000000", "/posters/shenhai2.svg"},
            {"射雕英雄传：侠之大者", "徐克", "肖战、庄达菲", "武侠/动作", "155", "中文", "2026-01-20", "郭靖和黄蓉联手守卫襄阳城，抵御外敌入侵。", "linear-gradient(135deg, #5c3a0e 0%, #8b5e3c 25%, #a07040 50%, #c09060 75%, #d4a870 100%)", "8.6", "中国", "false", "3200000000", "/posters/shediao.svg"},
            {"蜘蛛侠：纵横宇宙2", "乔伊姆·多斯·桑托斯", "沙梅克·摩尔、海莉·斯坦菲尔德", "动画/动作", "140", "英语", "2026-03-20", "迈尔斯·莫拉莱斯穿越多元宇宙，与来自不同时空的蜘蛛侠们组队。", "linear-gradient(135deg, #cc0000 0%, #e62e2e 25%, #1a1aff 50%, #0000cc 75%, #000066 100%)", "9.1", "美国", "true", "4500000000", "/posters/zhizhuxia2.svg"},
            {"阿凡达3：火与灰", "詹姆斯·卡梅隆", "萨姆·沃辛顿、佐伊·索尔达娜", "科幻/冒险", "190", "英语", "2026-04-01", "杰克和奈蒂莉遇到了纳美人的火族部落。", "linear-gradient(135deg, #cc3300 0%, #ff4d1a 25%, #ff6633 50%, #ff9933 75%, #ffcc00 100%)", "9.3", "美国", "true", "3500000000", "/posters/afanda3.svg"},
        };
        for (String[] m : movies) {
            Movie movie = new Movie();
            movie.setTitle(m[0]); movie.setDirector(m[1]); movie.setCast(m[2]); movie.setGenre(m[3]);
            movie.setDuration(Integer.parseInt(m[4])); movie.setLanguage(m[5]); movie.setReleaseDate(m[6]);
            movie.setDescription(m[7]); movie.setPosterBg(m[8]); movie.setRating(Double.parseDouble(m[9]));
            movie.setCountry(m[10]); movie.setIsHot(Boolean.parseBoolean(m[11]));
            movie.setBoxOffice(Double.parseDouble(m[12])); movie.setPosterUrl(m[13]);
            movie.setStatus("showing"); movieRepository.save(movie);
        }
    }

    private void addComingMovies() {
        String[][] comingMovies = {
            {"侏罗纪世界4：新生代", "加里斯·爱德华斯", "克里斯·帕拉特、布莱丝·达拉斯·霍华德", "科幻/冒险", "148", "英语", "2026-06-15", "恐龙与人类共存的新时代来临。", "linear-gradient(135deg, #1a3a1a 0%, #2d6b2d 25%, #4a9a4a 50%, #6ab86a 75%, #8fd88f 100%)", "美国", "false", "/posters/jurassic4.svg"},
            {"速度与激情12", "路易斯·莱特里尔", "范·迪塞尔、杰森·斯坦森", "动作/犯罪", "155", "英语", "2026-06-28", "唐老大团队面对前所未有的全球威胁。", "linear-gradient(135deg, #2a1a0a 0%, #553311 25%, #885522 50%, #aa7744 75%, #cc9955 100%)", "美国", "false", "/posters/fast12.svg"},
            {"大闹天宫之齐天大圣", "田晓鹏", "张磊、杨天翔", "动画/神话", "125", "中文", "2026-07-01", "孙悟空从灵石中诞生，拜师学艺，大闹龙宫，踏碎凌霄。", "linear-gradient(135deg, #cc2200 0%, #ee4411 25%, #ff6633 50%, #ff8833 75%, #ffdd44 100%)", "中国", "true", "/posters/wukong.svg"},
            {"星际穿越2：彼岸", "克里斯托弗·诺兰", "马修·麦康纳、安妮·海瑟薇", "科幻/剧情", "175", "英语", "2026-07-10", "库珀穿越虫洞后的第十年，人类在新家园发现了来自另一个维度的信号。", "linear-gradient(135deg, #000022 0%, #000044 25%, #111166 50%, #333388 75%, #5555aa 100%)", "美国", "true", "/posters/interstellar2.svg"},
            {"无名之辈2", "饶晓志", "陈建斌、任素汐、章宇", "喜剧/剧情", "118", "中文", "2026-07-18", "几个毫无交集的小人物因为一桩离奇事件凑到一起。", "linear-gradient(135deg, #333333 0%, #555555 25%, #777777 50%, #999999 75%, #bbbbbb 100%)", "中国", "false", "/posters/wuming2.svg"},
            {"毒液3：共生之怒", "凯莉·马塞尔", "汤姆·哈迪、朱诺·坦普尔", "动作/科幻", "130", "英语", "2026-07-25", "埃迪和毒液面对迄今为止最强大的敌人。", "linear-gradient(135deg, #111111 0%, #222244 25%, #333366 50%, #444488 75%, #555599 100%)", "美国", "false", "/posters/venom3.svg"},
            {"长安十二时辰", "曹盾", "易烊千玺、雷佳音", "悬疑/历史", "140", "中文", "2026-07-30", "天宝三载，长安城。一名死囚被临时释放。", "linear-gradient(135deg, #8b4513 0%, #a0522d 25%, #b8860b 50%, #cd853f 75%, #daa520 100%)", "中国", "true", "/posters/changan.svg"},
            {"功夫熊猫5", "迈克·米切尔", "杰克·布莱克、安吉丽娜·朱莉", "动画/喜剧", "105", "英语", "2026-06-20", "阿宝收了一个天赋异禀但桀骜不驯的徒弟。", "linear-gradient(135deg, #cc4400 0%, #ee6622 25%, #ff9944 50%, #ffbb66 75%, #ffdd88 100%)", "美国", "false", "/posters/kungfu5.svg"},
        };
        for (String[] m : comingMovies) {
            Movie movie = new Movie();
            movie.setTitle(m[0]); movie.setDirector(m[1]); movie.setCast(m[2]); movie.setGenre(m[3]);
            movie.setDuration(Integer.parseInt(m[4])); movie.setLanguage(m[5]); movie.setReleaseDate(m[6]);
            movie.setDescription(m[7]); movie.setPosterBg(m[8]); movie.setRating(0.0);
            movie.setCountry(m[9]); movie.setIsHot(Boolean.parseBoolean(m[10]));
            movie.setBoxOffice(0.0); movie.setPosterUrl(m[11]);
            movie.setStatus("coming"); movieRepository.save(movie);
        }
        System.out.println("即将上映影片: " + comingMovies.length + " 部");
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return password;
        }
    }
}
