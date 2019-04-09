from sklearn import datasets
from sklearn.multiclass import OneVsRestClassifier
from sklearn.svm import LinearSVC
from sklearn.cross_validation import train_test_split,cross_val_score
from sklearn.svm import SVC
from sklearn.linear_model import LogisticRegression
from xgboost.sklearn import XGBClassifier
import sklearn
import numpy as np
from sklearn.preprocessing import OneHotEncoder
from sklearn.metrics import precision_score,roc_auc_score
import os
os.environ["CUDA_VISIBLE_DEVICES"] = "-1"

min_max_scaler = sklearn.preprocessing.MinMaxScaler(feature_range=(0, 1))
resultX = []
resultY = []
with open("../data.txt", 'r') as rf:
    train_lines = rf.readlines()
    for train_line in train_lines:
        train_line = train_line.strip("\n")
        train_line_temp = train_line.split(" ")
        train_line_temp = list(map(float, train_line_temp))
        line_x = train_line_temp[1:-1]
        line_y = train_line_temp[-1]
        resultX.append(line_x)
        resultY.append(line_y)

X = np.array(resultX)
Y = np.array(resultY)
X = min_max_scaler.fit_transform(X)
Y = OneHotEncoder(sparse= False).fit_transform(Y.reshape(-1, 1))
X_train, X_test, Y_train, Y_test = train_test_split(X, Y, test_size=0.3)

model = OneVsRestClassifier(XGBClassifier(), n_jobs=2)
clf = model.fit(X_train, Y_train)

pre_Y = clf.predict(X_test)
test_auc2 = roc_auc_score(Y_test,pre_Y)#验证集上的auc值
print("xgb_muliclass_auc:", test_auc2)
