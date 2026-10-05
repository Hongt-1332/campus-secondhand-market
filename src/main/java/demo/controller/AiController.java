package demo.controller;

import com.alibaba.fastjson2.JSON;
import demo.common.Result;
import demo.entity.Product;
import demo.mapper.ProductMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {
    // 从环境变量读取 API Key，避免密钥写入代码/远程仓库
    private final String API_KEY = System.getenv("DASHSCOPE_API_KEY");
    private final String TONGYI_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private final RestTemplate restTemplate;
    private final ProductMapper productMapper;

    public AiController(RestTemplate restTemplate, ProductMapper productMapper) {
        this.restTemplate = restTemplate;
        this.productMapper = productMapper;
    }

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody Map<String, Object> params) {
        List<Map<String, String>> messages = (List<Map<String, String>>) params.get("messages");
        String userContent = "";
        for (Map<String, String> msg : messages) {
            if ("user".equals(msg.get("role"))) {
                userContent = msg.get("content");
            }
        }

        // 查询商品
        List<Product> productList = productMapper.selectOnSaleList();
        String productText = buildProductText(productList);

        // 调试打印
        System.out.println("========== 传给AI的商品清单 ==========");
        System.out.println(productText);
        System.out.println("======================================");

        // 强约束提示词，支持模糊匹配
        String systemPrompt = """
    你是校园二手交易平台专属智能客服，严格遵守以下规则：
    1. 只能使用下方【平台在售商品清单】的信息回答，绝对不能用自身外部知识，不能科普产品、编造商品。
    2. 用户提问的关键词和清单里商品名称/卖家名称匹配，回复对应商品的名称、价格、卖家、联系方式。
    3. 若清单中标注“卖家暂未填写联系方式”，统一回复：该卖家暂未填写公开联系方式，请前往平台商品页面私信联系对方。
    4. 清单里完全没有匹配商品时，固定回复：“当前平台暂无该类商品，您可以发布求购信息或浏览其他在售商品。”
    5. 禁止直接输出null、空白等原始数据库标识，必须替换为友好中文提示。
    
    【平台在售商品清单】
    %s
    """.formatted(productText);
        // 组装请求
        Map<String, Object> body = new HashMap<>();
        body.put("model", "qwen-turbo");
        Map<String, Object> input = new HashMap<>();
        List<Map<String, String>> msgList = new ArrayList<>();

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        msgList.add(systemMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userContent);
        msgList.add(userMsg);

        input.put("messages", msgList);
        body.put("input", input);

        Map<String, Float> parameters = new HashMap<>();
        parameters.put("temperature", 0.1f);
        body.put("parameters", parameters);

        // 调用接口
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);
        HttpEntity<String> request = new HttpEntity<>(JSON.toJSONString(body), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(TONGYI_URL, request, String.class);
            Map<String, Object> resObj = JSON.parseObject(response.getBody(), Map.class);
            Map<String, Object> output = (Map<String, Object>) resObj.get("output");
            String answer = (String) output.get("text");
            return Result.success(answer);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("AI调用失败：" + e.getMessage());
        }
    }

    private String buildProductText(List<Product> list) {
        if (list == null || list.isEmpty()) {
            return "暂无在售商品";
        }
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (Product p : list) {
            // 空值兜底
            String contactText = p.getContact() == null || p.getContact().isBlank() ? "卖家暂未填写联系方式" : p.getContact();
            sb.append(index).append(". ")
                    .append("商品名称：").append(p.getName()).append("，")
                    .append("价格：").append(p.getPrice()).append("元，")
                    .append("卖家：").append(p.getSeller()).append("，")
                    .append("卖家联系方式：").append(contactText).append("\n");
            index++;
        }
        return sb.toString();
    }
}