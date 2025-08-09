package cn.y.usercenter.service;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class InsertUserTest {

//    @Resource
//    private UserService userService;
//
//    // CPU 密集型：分配的核心线程数 = CPU - 1
//    // IO 密集型：分配的核心线程数可以大于 CPU 核数
//    private ExecutorService executorService = new ThreadPoolExecutor(60, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));
//
//    @Test
//    public void doInsertUser() {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//        final int INSERT_NUM = 100000;
//        List<User> userList = new ArrayList<>();
//        for (int i = 0; i < INSERT_NUM; i++) {
//            User user = new User();
//            user.setUsername("假沙鱼");
//            user.setUserAccount("yusha");
//            user.setAvatarUrl("shanghai.myqcloud.com/shayu931/shayu.png");
//            user.setProfile("一条咸鱼");
//            user.setGender(0);
//            user.setUserPassword("12345678");
//            user.setPhone("123456789108");
//            user.setEmail("shayu-yusha@qq.com");
//            user.setUserStatus(0);
//            user.setUserRole(0);
//            user.setPlanetCode("931");
//            user.setTags("[]");
//            userList.add(user);
//        }
//        userService.saveBatch(userList,1000);
//        stopWatch.stop();
//        System.out.println( stopWatch.getLastTaskTimeMillis());
//
//    }
//
//
//    /**
//     * 并发批量插入数据
//     */
//    @Test
//    public void doConcurrencyInsertUser() {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//        final int Batch_Size = 2500;
//        List<CompletableFuture<Void>> futureList = new ArrayList<>();
//        // 分十组
//        int j = 0;
//        for (int i = 0; i < 40; i++) {
//            List<User> userList = new ArrayList<>();
//            while(true){
//                j++;
//                User user = new User();
//                user.setUsername("假沙鱼");
//                user.setUserAccount("yusha");
//                user.setAvatarUrl("shanghai.myqcloud.com/shayu931/shayu.png");
//                user.setProfile("一条咸鱼");
//                user.setGender(0);
//                user.setUserPassword("12345678");
//                user.setPhone("123456789108");
//                user.setEmail("shayu-yusha@qq.com");
//                user.setUserStatus(0);
//                user.setUserRole(0);
//                user.setPlanetCode("931");
//                user.setTags("[]");
//                userList.add(user);
//                if(j % Batch_Size == 0){
//                    break;
//                }
//            }
//            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                System.out.println("threadName: " + Thread.currentThread().getName());
//                userService.saveBatch(userList, 10000);
//            }, executorService);
//            futureList.add(future);
//        }
//        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
//
//        stopWatch.stop();
//        System.out.println( stopWatch.getLastTaskTimeMillis());
//
//    }

}
