<template>
  <div class="page">
    <h2>紧急联系人</h2>
    <p class="description">管理您的紧急联系人信息，确保在需要时能够及时联系到相关人员</p>
    
    <!-- 新增/编辑表单 -->
    <div class="form-section">
      <h3>{{ editingId ? '编辑联系人' : '添加新联系人' }}</h3>
      <form @submit.prevent="onSubmit">
        <div class="form-row">
          <input v-model="form.name" placeholder="姓名" required />
          <input v-model="form.phone" placeholder="电话" required />
          <input v-model="form.relation" placeholder="关系" />
        </div>
        <div class="form-actions">
          <label class="checkbox-label">
            <input type="checkbox" v-model="form.isPrimary" /> 
            设为主联系人
          </label>
          <div class="buttons">
            <button type="submit" class="btn-primary">
              {{ editingId ? '更新' : '保存' }}
            </button>
            <button v-if="editingId" type="button" @click="cancelEdit" class="btn-secondary">
              取消
            </button>
          </div>
        </div>
      </form>
    </div>

    <!-- 联系人列表 -->
    <div class="contacts-section">
      <h3>联系人列表</h3>
      <div v-if="contacts.length === 0" class="empty-state">
        <p>暂无紧急联系人，请添加您的紧急联系人信息</p>
      </div>
      <ul v-else class="contacts-list">
        <li v-for="c in contacts" :key="c.id" class="contact-item">
          <div class="contact-info">
            <span class="name">{{ c.name }}</span>
            <span class="relation">{{ c.relation || '联系人' }}</span>
            <span class="phone">{{ c.phone }}</span>
            <span v-if="c.isPrimary === 1" class="primary-badge">主联系人</span>
          </div>
          <div class="contact-actions">
            <button @click="edit(c)" class="btn-edit">编辑</button>
            <button v-if="c.isPrimary !== 1" @click="setPrimary(c)" class="btn-primary-action">设为主</button>
            <button @click="remove(c)" class="btn-delete">删除</button>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listEmergencyContacts, createEmergencyContact, updateEmergencyContact, deleteEmergencyContact, setPrimaryEmergencyContact } from '@/api/safety'

const contacts = ref<any[]>([])
const editingId = ref<number | null>(null)
const form = ref<any>({ name: '', phone: '', relation: '', isPrimary: false })

async function load() {
  try {
    const res = await listEmergencyContacts()
    console.log('紧急联系人API响应:', res) // 调试日志
    contacts.value = res.data || res || []
  } catch (error) {
    console.error('加载紧急联系人失败:', error)
    contacts.value = []
  }
}

function edit(c: any) {
  editingId.value = c.id
  form.value = { name: c.name, phone: c.phone, relation: c.relation, isPrimary: c.isPrimary === 1 }
}

function cancelEdit() {
  editingId.value = null
  form.value = { name: '', phone: '', relation: '', isPrimary: false }
}

async function onSubmit() {
  try {
    const payload = { ...form.value, isPrimary: form.value.isPrimary ? 1 : 0 }
    if (editingId.value) {
      await updateEmergencyContact(editingId.value, payload)
      console.log('更新紧急联系人成功')
    } else {
      await createEmergencyContact(payload)
      console.log('创建紧急联系人成功')
    }
    editingId.value = null
    form.value = { name: '', phone: '', relation: '', isPrimary: false }
    await load()
  } catch (error) {
    console.error('保存紧急联系人失败:', error)
  }
}

async function remove(c: any) {
  try {
    await deleteEmergencyContact(c.id)
    console.log('删除紧急联系人成功')
    await load()
  } catch (error) {
    console.error('删除紧急联系人失败:', error)
  }
}

async function setPrimary(c: any) {
  try {
    await setPrimaryEmergencyContact(c.id)
    console.log('设置主联系人成功')
    await load()
  } catch (error) {
    console.error('设置主联系人失败:', error)
  }
}

onMounted(load)
</script>

<style scoped>
.page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.page h2 {
  color: #2c3e50;
  margin-bottom: 8px;
}

.description {
  color: #666;
  margin-bottom: 30px;
  font-size: 14px;
}

.form-section, .contacts-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.form-section h3, .contacts-section h3 {
  color: #34495e;
  margin-bottom: 15px;
  font-size: 16px;
}

.form-row {
  display: flex;
  gap: 12px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.form-row input {
  flex: 1;
  min-width: 150px;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-row input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #555;
}

.checkbox-label input[type="checkbox"] {
  width: 16px;
  height: 16px;
}

.buttons {
  display: flex;
  gap: 10px;
}

.btn-primary {
  background: #3498db;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary:hover {
  background: #2980b9;
}

.btn-secondary {
  background: #95a5a6;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-secondary:hover {
  background: #7f8c8d;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.contacts-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.contact-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 6px;
  margin-bottom: 10px;
  background: #fafafa;
}

.contact-info {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.contact-info .name {
  font-weight: 600;
  color: #2c3e50;
}

.contact-info .relation {
  color: #7f8c8d;
  font-size: 14px;
}

.contact-info .phone {
  color: #34495e;
  font-family: monospace;
}

.primary-badge {
  background: #e74c3c;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.contact-actions {
  display: flex;
  gap: 8px;
}

.btn-edit {
  background: #f39c12;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-edit:hover {
  background: #e67e22;
}

.btn-primary-action {
  background: #27ae60;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-primary-action:hover {
  background: #229954;
}

.btn-delete {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-delete:hover {
  background: #c0392b;
}

@media (max-width: 600px) {
  .form-row {
    flex-direction: column;
  }
  
  .form-row input {
    min-width: auto;
  }
  
  .form-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .contact-item {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .contact-info {
    justify-content: center;
  }
  
  .contact-actions {
    justify-content: center;
  }
}
</style>


