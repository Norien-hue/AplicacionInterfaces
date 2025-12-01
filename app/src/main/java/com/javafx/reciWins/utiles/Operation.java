// // Operation.java
// package com.javafx.reciWins.utiles;

// public class Operation<T> {
//     public enum Type { INSERT, UPDATE, DELETE }
    
//     private Type type;
//     private T entity;
//     private T oldEntity; // Para UPDATE
    
//     public Operation(Type type, T entity) {
//         this.type = type;
//         this.entity = entity;
//     }
    
//     public Operation(Type type, T entity, T oldEntity) {
//         this.type = type;
//         this.entity = entity;
//         this.oldEntity = oldEntity;
//     }

//     public Type getType() {
//         return type;
//     }

//     public void setType(Type type) {
//         this.type = type;
//     }

//     public T getEntity() {
//         return entity;
//     }

//     public void setEntity(T entity) {
//         this.entity = entity;
//     }

//     public T getOldEntity() {
//         return oldEntity;
//     }

//     public void setOldEntity(T oldEntity) {
//         this.oldEntity = oldEntity;
//     }
// }