# 📌 Visit Creation - Validation Flow

Este documento describe el flujo completo de validaciones para la creación de una `Visit`, organizado por orden de ejecución y separado entre validaciones de **input (usuario)** y de **negocio (dominio)**.

---

## 🧱 1. Validaciones de Input (DTO / Request)

Estas validaciones deben ejecutarse automáticamente mediante anotaciones (ej: Bean Validation).

### Reglas

- **shiftId**
    - No puede ser `null`

- **totalAmount**
    - No puede ser `null`
    - Debe ser mayor o igual a `0`

- **paymentStatus**
    - No puede ser `null`

- **currency**
    - Es opcional
    - Si se envía:
        - Debe tener exactamente 3 caracteres
        - Se recomienda normalizar a mayúsculas (`ARS`, `USD`)

---

## 🔍 2. Validaciones de Existencia

### Reglas

- El `Shift` debe existir

### Error
- "This shift does not exist."

---

## 🔒 3. Validaciones de Integridad de Relación

### Reglas

- El `Shift` no debe tener ya una `Visit` asociada (relación 1:1)

### Error
-"This shift already has a visit associated."

---

## 🧠 4. Validaciones de Negocio (Shift)

### Reglas

- El `Shift` debe estar en un estado válido para generar una visita

### Recomendación

- Permitir solo si: ```ShiftStatus == COMPLETED```

### Error
- "A visit can only be created for a completed shift."

---

## 💰 5. Validaciones del Monto

### Reglas

- `totalAmount >= 0`

- Si: ```paymentStatus == PAID```

Entonces:

- `totalAmount > 0`

### Error
- "Total amount must be greater than zero when payment status is PAID."

---

## 💳 6. Validaciones de Consistencia de Pago

### Caso: `PAID`

- `paidAt` → obligatorio
- `paymentMethod` → obligatorio
- `totalAmount > 0`

### Errores
- "paidAt is required when payment status is PAID."
- "paymentMethod is required when payment status is PAID."

---

### Caso: `PENDING`

- `paidAt` → debe ser `null`
- `paymentMethod` → debe ser `null`

### Errores
- "paidAt must be null when payment status is PENDING."
- "paymentMethod must be null when payment status is PENDING."

---

### Caso: `PARTIAL` *(limitado por modelo actual)*

- Validación básica
- No hay suficiente información para lógica completa

### Recomendación futura

Agregar: ```paidAmount```

---

### Caso: `REFUNDED` *(opcional según negocio)*

- Puede requerir validaciones adicionales en el futuro
- Por ahora, mantener lógica simple

---

## ⏱️ 7. Validaciones Temporales

### Reglas

- `paidAt` no puede estar en el futuro

### Error
- "paidAt cannot be in the future."

---

### Regla opcional

- `paidAt` no debería ser anterior al turno

⚠️ Solo aplicar si el negocio no permite pagos anticipados

---

## 🔧 8. Normalización de Datos

Antes de persistir:

- `currency`:
    - `trim()`
    - `toUpperCase()`

---

## 🧩 9. Orden Final de Ejecución
1. Validaciones de input (DTO)

2. Buscar Shift (existencia)

3. Validar que no tenga Visit

4. Validar estado del Shift

5. Validar monto

6. Validar consistencia de pago

7. Validar fechas (paidAt)

8. Normalizar datos

9. Crear y guardar Visit

---

## 🧠 Notas de Diseño

- Las validaciones de **input** deben implementarse en el DTO (ej: `@NotNull`, `@DecimalMin`, etc.)
- Las validaciones de **negocio** deben implementarse en el `Service`
- Evitar lógica compleja para `PARTIAL` y `REFUNDED` hasta modelarlas correctamente

---

## 🚀 Recomendaciones de Evolución

### Versión 1 (Actual)
- Estados: `PENDING`, `PAID`

### Versión 2
- Agregar soporte real para pagos parciales:
    - `paidAmount`

### Versión 3
- Soporte para reembolsos:
    - `refundAmount`
    - `refundedAt`

---