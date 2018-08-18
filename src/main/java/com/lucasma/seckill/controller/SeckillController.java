package com.lucasma.seckill.controller;

import com.lucasma.seckill.access.AccessLimit;
import com.lucasma.seckill.domain.OrderInfo;
import com.lucasma.seckill.domain.SeckillOrder;
import com.lucasma.seckill.domain.SeckillUser;
import com.lucasma.seckill.result.CodeMsg;
import com.lucasma.seckill.result.Result;
import com.lucasma.seckill.service.GoodsService;
import com.lucasma.seckill.service.OrderService;
import com.lucasma.seckill.service.SeckillService;
import com.lucasma.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;

/**
 * Author: lucasma
 *
 *
 *  秒杀控制层
 */
@Controller
@RequestMapping("/seckill")
public class SeckillController {


    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SeckillService seckillService;


    @RequestMapping("do_seckill")
    public String list(Model model, SeckillUser user, @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return "login";
        }
        // 库存判断
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock < 0) {
            model.addAttribute("errmsg", CodeMsg.SECKILL_OVER.getMsg());
            return "seckill_fail";
        }

        // 判断是否秒杀到了
       SeckillOrder order =  orderService.getSeckillOrderByUserIdGoodsId(user.getId(),goodsId);
        if (order != null) {
            model.addAttribute("errmsg", CodeMsg.REPEATE_SECKILL.getMsg());
            return "seckill_fail";
        }
        // 减库存，下订单，记录秒杀
        OrderInfo orderInfo = seckillService.seckill(user,goods);
        model.addAttribute("orderInfo", orderInfo);
        model.addAttribute("goods", goods);
        return "order_detail";

    }


    @AccessLimit(seconds=5, maxCount=5, needLogin=true)
    @RequestMapping(value="/path", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest request, SeckillUser user,
                                         @RequestParam("goodsId")long goodsId,
                                         @RequestParam(value="verifyCode", defaultValue="0")int verifyCode
    ) {
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        boolean check = seckillService.checkVerifyCode(user, goodsId, verifyCode);
        if(!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        String path  =seckillService.createSeckillPath(user, goodsId);
        return Result.success(path);
    }


    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCod(HttpServletResponse response, SeckillUser user,
                                              @RequestParam("goodsId")long goodsId) {
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        try {
            BufferedImage image  = seckillService.createVerifyCode(user, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        }catch(Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.SECKILL_FAIL);
        }
    }

}
