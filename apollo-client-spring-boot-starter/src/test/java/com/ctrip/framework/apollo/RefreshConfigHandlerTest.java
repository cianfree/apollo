package com.ctrip.framework.apollo;

import com.ctrip.framework.apollo.annotations.AbstractConfigRefreshHandler;
import com.ctrip.framework.apollo.annotations.ConfigRefreshHandler;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Arvin
 * @since 2019-07-12
 */
public class RefreshConfigHandlerTest {

    @Test
    public void testRefreshConfigHandlerSort() {

        class HandlerOne extends AbstractConfigRefreshHandler {

            @Override
            public boolean accept(ConfigChangeEvent changeEvent, String beanName, Object bean) {
                return false;
            }

            @Override
            public int getHandlerOrder() {
                return 2;
            }
        }

        class HandlerTwo extends AbstractConfigRefreshHandler {

            @Override
            public boolean accept(ConfigChangeEvent changeEvent, String beanName, Object bean) {
                return false;
            }

            @Override
            public int getHandlerOrder() {
                return Integer.MIN_VALUE;
            }
        }

        List<ConfigRefreshHandler> handlers = new ArrayList<>();
        handlers.add(new HandlerOne());
        handlers.add(new HandlerTwo());

        System.out.println("Before Order: ");
        showHandlers(handlers);

        Collections.sort(handlers, new Comparator<ConfigRefreshHandler>() {
            @Override
            public int compare(ConfigRefreshHandler o1, ConfigRefreshHandler o2) {
                return Integer.compare(o1.getHandlerOrder(), o2.getHandlerOrder());
            }
        });

        System.out.println("After Order: ");
        showHandlers(handlers);
    }

    private void showHandlers(List<ConfigRefreshHandler> handlers) {
        for (ConfigRefreshHandler handler : handlers) {
            System.out.print(handler.getHandlerOrder() + ":" + handler.getClass().getSimpleName() + ", ");
        }
        System.out.println();
    }
}
