/*
 * Copyright (c) 2011-2020, baomidou (jobob@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.baomidou.mybatisplus.generator.config.builder;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.ClassUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.config.INameConvert;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.function.ConverterFileName;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 实体属性配置
 *
 * @author nieqiurong 2020/10/11.
 * @since 3.4.1
 */
public class Entity {

    private Entity() {

    }

    /**
     * 名称转换
     */
    private INameConvert nameConvert;

    /**
     * 自定义继承的Entity类全称，带包名
     */
    private String superClass;

    /**
     * 自定义基础的Entity类，公共字段
     */
    private final Set<String> superEntityColumns = new HashSet<>();

    /**
     * 实体是否生成 serialVersionUID
     */
    private boolean serialVersionUID;

    /**
     * 【实体】是否生成字段常量（默认 false）<br>
     * -----------------------------------<br>
     * public static final String ID = "test_id";
     */
    private boolean columnConstant;

    /**
     * 【实体】是否为链式模型（默认 false）<br>
     * -----------------------------------<br>
     * public User setName(String name) { this.name = name; return this; }
     *
     * @since 3.3.2
     */
    private boolean chain;

    /**
     * 【实体】是否为lombok模型（默认 false）<br>
     * <a href="https://projectlombok.org/">document</a>
     */
    private boolean lombok;

    /**
     * Boolean类型字段是否移除is前缀（默认 false）<br>
     * 比如 : 数据库字段名称 : 'is_xxx',类型为 : tinyint. 在映射实体的时候则会去掉is,在实体类中映射最终结果为 xxx
     */
    private boolean booleanColumnRemoveIsPrefix;

    /**
     * 是否生成实体时，生成字段注解
     */
    private boolean tableFieldAnnotationEnable;


    /**
     * 乐观锁字段名称(数据库字段)
     *
     * @since 3.4.1
     */
    private String versionColumnName;

    /**
     * 乐观锁属性名称(实体字段)
     *
     * @since 3.4.1
     */
    private String versionPropertyName;

    /**
     * 逻辑删除属性数据库字段名称
     * @since 3.4.1
     */
    private String logicDeleteColumnName;

    /**
     * 逻辑删除实体属性名称
     * @since 3.4.1
     */
    private String logicDeletePropertyName;

    /**
     * 表填充字段
     */
    private final List<TableFill> tableFillList = new ArrayList<>();

    /**
     * 数据库表映射到实体的命名策略
     */
    private NamingStrategy naming = NamingStrategy.no_change;

    /**
     * 数据库表字段映射到实体的命名策略
     * <p>未指定按照 naming 执行</p>
     */
    private NamingStrategy columnNaming = null;

    /**
     * 开启 ActiveRecord 模式
     *
     * @since 3.4.1
     */
    private boolean activeRecord;

    /**
     * 指定生成的主键的ID类型
     *
     * @since 3.4.1
     */
    private IdType idType;

    /**
     * 转换输出文件名称
     *
     * @since 3.4.1
     */
    private ConverterFileName converterFileName = (entityName -> entityName);

    /**
     * <p>
     * 父类 Class 反射属性转换为公共字段
     * </p>
     *
     * @param clazz 实体父类 Class
     */
    public void convertSuperEntityColumns(Class<?> clazz) {
        List<Field> fields = TableInfoHelper.getAllFields(clazz);
        this.superEntityColumns.addAll(fields.stream().map(field -> {
            TableId tableId = field.getAnnotation(TableId.class);
            if (tableId != null && StringUtils.isNotBlank(tableId.value())) {
                return tableId.value();
            }
            TableField tableField = field.getAnnotation(TableField.class);
            if (tableField != null && StringUtils.isNotBlank(tableField.value())) {
                return tableField.value();
            }
            if (null == columnNaming || columnNaming == NamingStrategy.no_change) {
                return field.getName();
            }
            return StringUtils.camelToUnderline(field.getName());
        }).collect(Collectors.toSet()));
    }

    public Set<String> getSuperEntityColumns() {
        if (StringUtils.isNotBlank(this.superClass)) {
            try {
                Class<?> superEntity = ClassUtils.toClassConfident(this.superClass);
                convertSuperEntityColumns(superEntity);
            } catch (Exception e) {
                //当父类实体存在类加载器的时候,识别父类实体字段，不存在的情况就只有通过指定superEntityColumns属性了。
            }
        }
        return this.superEntityColumns;
    }

    public NamingStrategy getColumnNaming() {
        // 未指定以 naming 策略为准
        return Optional.ofNullable(columnNaming).orElse(naming);
    }

    /**
     * 匹配父类字段(忽略大小写)
     *
     * @param fieldName 字段名
     * @return 是否匹配
     * @since 3.4.1
     */
    public boolean matchSuperEntityColumns(String fieldName) {
        // 公共字段判断忽略大小写【 部分数据库大小写不敏感 】
        return superEntityColumns.stream().anyMatch(e -> e.equalsIgnoreCase(fieldName));
    }

    /**
     * 获取乐观锁字段名称
     *
     * @return 乐观锁字段名称
     * @see #getVersionColumnName()
     * @deprecated 3.4.1
     */
    @Deprecated
    public String getVersionFieldName() {
        return getVersionColumnName();
    }

    /**
     * 获取逻辑删除字段名称
     *
     * @return 逻辑删除字段
     * @see #getLogicDeleteColumnName()
     * @deprecated 3.4.1
     */
    @Deprecated
    public String getLogicDeleteFieldName() {
        return getLogicDeleteColumnName();
    }

    public INameConvert getNameConvert() {
        return nameConvert;
    }

    public String getSuperClass() {
        return superClass;
    }

    public boolean isSerialVersionUID() {
        return serialVersionUID;
    }

    public boolean isColumnConstant() {
        return columnConstant;
    }

    public boolean isChain() {
        return chain;
    }

    public boolean isLombok() {
        return lombok;
    }

    public boolean isBooleanColumnRemoveIsPrefix() {
        return booleanColumnRemoveIsPrefix;
    }

    public boolean isTableFieldAnnotationEnable() {
        return tableFieldAnnotationEnable;
    }

    public String getVersionColumnName() {
        return versionColumnName;
    }

    public String getVersionPropertyName() {
        return versionPropertyName;
    }

    public String getLogicDeleteColumnName() {
        return logicDeleteColumnName;
    }

    public String getLogicDeletePropertyName() {
        return logicDeletePropertyName;
    }

    public List<TableFill> getTableFillList() {
        return tableFillList;
    }

    public NamingStrategy getNaming() {
        return naming;
    }

    public boolean isActiveRecord() {
        return activeRecord;
    }

    public IdType getIdType() {
        return idType;
    }

    public ConverterFileName getConverterFileName() {
        return converterFileName;
    }

    public static class Builder extends BaseBuilder {

        private final Entity entity = new Entity();

        public Builder(StrategyConfig strategyConfig) {
            super(strategyConfig);
            this.entity.nameConvert = new INameConvert.DefaultNameConvert(strategyConfig);
        }

        /**
         * 名称转换实现
         *
         * @param nameConvert 名称转换实现
         * @return this
         */
        public Builder nameConvert(INameConvert nameConvert) {
            this.entity.nameConvert = nameConvert;
            return this;
        }

        public Builder superClass(@NotNull Class<?> clazz) {
            return superClass(clazz.getName());
        }

        public Builder superClass(String superEntityClass) {
            this.entity.superClass = superEntityClass;
            return this;
        }

        /**
         * 实体是否生成serialVersionUID
         *
         * @param serialVersionUID 是否生成
         * @return this
         */
        public Builder serialVersionUID(boolean serialVersionUID) {
            this.entity.serialVersionUID = serialVersionUID;
            return this;
        }

        /**
         * 是否生成字段常量
         *
         * @param columnConstant 是否生成字段常量
         * @return this
         */
        public Builder columnConstant(boolean columnConstant) {
            this.entity.columnConstant = columnConstant;
            return this;
        }

        /**
         * 实体是否为链式模型
         *
         * @param chain 是否为链式模型
         * @return this
         */
        public Builder chainModel(boolean chain) {
            this.entity.chain = chain;
            return this;
        }

        /**
         * 是否为lombok模型
         *
         * @param lombok 是否为lombok模型
         * @return this
         */
        public Builder lombok(boolean lombok) {
            this.entity.lombok = lombok;
            return this;
        }

        /**
         * Boolean类型字段是否移除is前缀
         *
         * @param booleanColumnRemoveIsPrefix 是否移除
         * @return this
         */
        public Builder booleanColumnRemoveIsPrefix(boolean booleanColumnRemoveIsPrefix) {
            this.entity.booleanColumnRemoveIsPrefix = booleanColumnRemoveIsPrefix;
            return this;
        }

        /**
         * 生成实体时，是否生成字段注解
         *
         * @param tableFieldAnnotationEnable 是否生成
         * @return this
         */
        public Builder tableFieldAnnotationEnable(boolean tableFieldAnnotationEnable) {
            this.entity.tableFieldAnnotationEnable = tableFieldAnnotationEnable;
            return this;
        }

        /**
         * 乐观锁属性名称
         *
         * @param versionFieldName 乐观锁属性名称
         * @return this
         * @see #versionColumnName(String)
         */
        @Deprecated
        public Builder versionFieldName(String versionFieldName) {
            return versionColumnName(versionFieldName);
        }

        /**
         * 设置乐观锁数据库表字段名称
         *
         * @param versionColumnName 乐观锁数据库字段名称
         * @return this
         */
        public Builder versionColumnName(String versionColumnName) {
            this.entity.versionColumnName = versionColumnName;
            return this;
        }

        /**
         * 设置乐观锁实体属性字段名称
         *
         * @param versionPropertyName 乐观锁实体属性字段名称
         * @return this
         */
        public Builder versionPropertyName(String versionPropertyName) {
            this.entity.versionPropertyName = versionPropertyName;
            return this;
        }

        /**
         * 逻辑删除属性名称
         *
         * @param logicDeleteFieldName 逻辑删除属性名称
         * @return this
         * @deprecated 3.4.1
         */
        @Deprecated
        public Builder logicDeleteFieldName(String logicDeleteFieldName) {
            return logicDeleteColumnName(logicDeleteFieldName);
        }

        /**
         * 逻辑删除数据库字段名称
         *
         * @param logicDeleteColumnName 逻辑删除字段名称
         * @return this
         */
        public Builder logicDeleteColumnName(String logicDeleteColumnName) {
            this.entity.logicDeleteColumnName = logicDeleteColumnName;
            return this;
        }

        /**
         * 逻辑删除实体属性名称
         *
         * @param logicDeletePropertyName 逻辑删除实体属性名称
         * @return this
         */
        public Builder logicDeletePropertyName(String logicDeletePropertyName) {
            this.entity.logicDeletePropertyName = logicDeletePropertyName;
            return this;
        }

        public Builder naming(NamingStrategy namingStrategy) {
            this.entity.naming = namingStrategy;
            return this;
        }

        public Builder columnNaming(NamingStrategy namingStrategy) {
            this.entity.columnNaming = namingStrategy;
            return this;
        }

        /**
         * 添加父类公共字段
         *
         * @param superEntityColumns 父类字段(数据库字段列名)
         * @return this
         * @since 3.4.1
         */
        public Builder addSuperEntityColumns(@NotNull String... superEntityColumns) {
            this.entity.superEntityColumns.addAll(Arrays.asList(superEntityColumns));
            return this;
        }

        /**
         * 添加表字段填充
         *
         * @param tableFill 填充字段
         * @return this
         * @since 3.4.1
         */
        public Builder addTableFills(@NotNull TableFill... tableFill) {
            this.entity.tableFillList.addAll(Arrays.asList(tableFill));
            return this;
        }

        /**
         * 添加表字段填充
         *
         * @param tableFillList 填充字段集合
         * @return this
         * @since 3.4.1
         */
        public Builder addTableFills(@NotNull List<TableFill> tableFillList) {
            this.entity.tableFillList.addAll(tableFillList);
            return this;
        }

        /**
         * 开启 ActiveRecord 模式
         *
         * @param activeRecord 是否开启
         * @return this
         * @since 3.4.1
         */
        public Builder activeRecord(boolean activeRecord) {
            this.entity.activeRecord = activeRecord;
            return this;
        }

        /**
         * 指定生成的主键的ID类型
         *
         * @param idType ID类型
         * @return this
         * @since 3.4.1
         */
        public Builder idType(IdType idType) {
            this.entity.idType = idType;
            return this;
        }

        /**
         * 转换输出文件名称
         *
         * @param converter 　转换处理
         * @return this
         * @since 3.4.1
         */
        public Builder convertFileName(@NotNull ConverterFileName converter) {
            this.entity.converterFileName = converter;
            return this;
        }

        /**
         * 格式化文件名称
         *
         * @param format 　格式
         * @return this
         * @since 3.4.1
         */
        public Builder formatFileName(String format) {
            return convertFileName((entityName) -> String.format(format, entityName));
        }

        public Entity get(){
            return this.entity;
        }
    }
}
